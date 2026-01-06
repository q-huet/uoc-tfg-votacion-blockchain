import { TestBed } from '@angular/core/testing';
import { HttpRequest } from '@angular/common/http';
import { of } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { jwtInterceptor } from './jwt.interceptor';

describe('jwtInterceptor', () => {
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['getToken']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService }
      ]
    });
  });

  it('should add Authorization header when token exists and URL is not public', (done) => {
    const token = 'test-token-123';
    authService.getToken.and.returnValue(token);
    
    const mockRequest = new HttpRequest('GET', 'http://localhost:8080/api/v1/elections');
    const mockNext = jasmine.createSpy('next').and.returnValue(of({}));
    
    TestBed.runInInjectionContext(() => {
      jwtInterceptor(mockRequest, mockNext);
      
      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.headers.get('Authorization')).toBe(`Bearer ${token}`);
      done();
    });
  });

  it('should not add Authorization header for login URL', (done) => {
    authService.getToken.and.returnValue('test-token-123');
    
    const mockRequest = new HttpRequest('POST', 'http://localhost:8080/api/v1/auth/login', {});
    const mockNext = jasmine.createSpy('next').and.returnValue(of({}));
    
    TestBed.runInInjectionContext(() => {
      jwtInterceptor(mockRequest, mockNext);
      
      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.headers.has('Authorization')).toBeFalse();
      done();
    });
  });

  it('should not add Authorization header when no token exists', (done) => {
    authService.getToken.and.returnValue(null);
    
    const mockRequest = new HttpRequest('GET', 'http://localhost:8080/api/v1/elections');
    const mockNext = jasmine.createSpy('next').and.returnValue(of({}));
    
    TestBed.runInInjectionContext(() => {
      jwtInterceptor(mockRequest, mockNext);
      
      const modifiedRequest = mockNext.calls.mostRecent().args[0];
      expect(modifiedRequest.headers.has('Authorization')).toBeFalse();
      done();
    });
  });
});
