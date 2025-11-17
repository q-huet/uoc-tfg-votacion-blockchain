import { TestBed } from '@angular/core/testing';
import { HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { errorInterceptor } from './error.interceptor';

describe('errorInterceptor', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['logout']);
    router = jasmine.createSpyObj('Router', ['navigate'], { url: '/test' });

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router }
      ]
    });
  });

  it('should handle 401 error and redirect to login', (done) => {
    const mockRequest = new HttpRequest('GET', 'http://localhost:8080/api/v1/elections');
    const errorResponse = new HttpErrorResponse({
      error: 'Unauthorized',
      status: 401,
      statusText: 'Unauthorized'
    });
    
    const mockNext = jasmine.createSpy('next').and.returnValue(throwError(() => errorResponse));
    
    TestBed.runInInjectionContext(() => {
      errorInterceptor(mockRequest, mockNext).subscribe({
        error: (error) => {
          expect(authService.logout).toHaveBeenCalled();
          expect(router.navigate).toHaveBeenCalledWith(
            ['/login'],
            { queryParams: { returnUrl: '/test', reason: 'session-expired' } }
          );
          expect(error.status).toBe(401);
          expect(error.message).toContain('Sesión expirada');
          done();
        }
      });
    });
  });

  it('should handle 403 error', (done) => {
    const mockRequest = new HttpRequest('GET', 'http://localhost:8080/api/v1/admin/users');
    const errorResponse = new HttpErrorResponse({
      error: 'Forbidden',
      status: 403,
      statusText: 'Forbidden'
    });
    
    const mockNext = jasmine.createSpy('next').and.returnValue(throwError(() => errorResponse));
    
    TestBed.runInInjectionContext(() => {
      errorInterceptor(mockRequest, mockNext).subscribe({
        error: (error) => {
          expect(error.status).toBe(403);
          expect(error.message).toContain('No tienes permisos');
          done();
        }
      });
    });
  });

  it('should handle 404 error', (done) => {
    const mockRequest = new HttpRequest('GET', 'http://localhost:8080/api/v1/elections/999');
    const errorResponse = new HttpErrorResponse({
      error: 'Not Found',
      status: 404,
      statusText: 'Not Found'
    });
    
    const mockNext = jasmine.createSpy('next').and.returnValue(throwError(() => errorResponse));
    
    TestBed.runInInjectionContext(() => {
      errorInterceptor(mockRequest, mockNext).subscribe({
        error: (error) => {
          expect(error.status).toBe(404);
          expect(error.message).toContain('Recurso no encontrado');
          done();
        }
      });
    });
  });

  it('should handle 500 error', (done) => {
    const mockRequest = new HttpRequest('POST', 'http://localhost:8080/api/v1/elections', {});
    const errorResponse = new HttpErrorResponse({
      error: 'Internal Server Error',
      status: 500,
      statusText: 'Internal Server Error'
    });
    
    const mockNext = jasmine.createSpy('next').and.returnValue(throwError(() => errorResponse));
    
    TestBed.runInInjectionContext(() => {
      errorInterceptor(mockRequest, mockNext).subscribe({
        error: (error) => {
          expect(error.status).toBe(500);
          expect(error.message).toContain('Error interno del servidor');
          done();
        }
      });
    });
  });

  it('should handle network error (status 0)', (done) => {
    const mockRequest = new HttpRequest('GET', 'http://localhost:8080/api/v1/elections');
    const errorResponse = new HttpErrorResponse({
      error: 'Network error',
      status: 0,
      statusText: 'Unknown Error'
    });
    
    const mockNext = jasmine.createSpy('next').and.returnValue(throwError(() => errorResponse));
    
    TestBed.runInInjectionContext(() => {
      errorInterceptor(mockRequest, mockNext).subscribe({
        error: (error) => {
          expect(error.status).toBe(0);
          expect(error.message).toContain('No se puede conectar al servidor');
          done();
        }
      });
    });
  });

  it('should pass through successful requests', (done) => {
    const mockRequest = new HttpRequest('GET', 'http://localhost:8080/api/v1/elections');
    const successResponse = { data: 'success' };
    const mockNext = jasmine.createSpy('next').and.returnValue(of(successResponse));
    
    TestBed.runInInjectionContext(() => {
      errorInterceptor(mockRequest, mockNext).subscribe({
        next: (response) => {
          expect(response).toEqual(jasmine.objectContaining({ data: 'success' }));
          expect(authService.logout).not.toHaveBeenCalled();
          expect(router.navigate).not.toHaveBeenCalled();
          done();
        }
      });
    });
  });
});
