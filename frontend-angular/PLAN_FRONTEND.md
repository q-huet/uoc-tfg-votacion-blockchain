# Plan de Desarrollo Frontend - Sistema de Votación Blockchain

## 📋 Visión General

El frontend está organizado por **roles de usuario** con interfaces específicas para cada tipo:
- **VOTER** (Votante): Votar en elecciones activas
- **ADMIN** (Administrador): Gestionar elecciones y usuarios
- **AUDITOR**: Ver resultados y auditar votos

## 🎯 Estructura de Páginas por Rol

### 🔐 Páginas Públicas (Sin autenticación)

#### 1. Login Page (`/login`)
**Estado**: ✅ IMPLEMENTADA
- [x] Formulario de login con username/password
- [x] Validación de campos
- [x] Manejo de errores
- [x] Redirección según rol después del login

**Componente**: `src/app/features/auth/login/`

---

### 👤 VOTER - Votante (Rol: VOTER)

#### 2. Dashboard Votante (`/voter/dashboard`)
**Prioridad**: ALTA
**Descripción**: Página principal del votante con resumen de elecciones

**Funcionalidades**:
- [ ] Lista de elecciones activas disponibles para votar
- [ ] Elecciones en las que ya ha votado (con fecha)
- [ ] Elecciones próximas (aún no abiertas)
- [ ] Indicador visual de estado: "Disponible", "Votado", "Próxima", "Cerrada"
- [ ] Filtros: Activas, Completadas, Todas
- [ ] Búsqueda por nombre de elección

**Componentes a crear**:
```
src/app/features/voter/
├── dashboard/
│   ├── dashboard.component.ts
│   ├── dashboard.component.html
│   └── dashboard.component.scss
└── components/
    ├── election-card/          # Card de elección
    └── vote-status-badge/      # Badge de estado
```

**API Endpoints**:
- `GET /api/v1/elections` - Lista de elecciones
- `GET /api/v1/elections/{id}` - Detalle de elección

---

#### 3. Detalle de Elección (`/voter/election/:id`)
**Prioridad**: ALTA
**Descripción**: Ver detalles completos de una elección antes de votar

**Funcionalidades**:
- [ ] Información completa de la elección (título, descripción, fechas)
- [ ] Lista de candidatos/opciones con descripción
- [ ] Información sobre el sistema de votación
- [ ] Botón "Votar" (solo si está activa y no ha votado)
- [ ] Mensaje si ya votó (con fecha y hora)
- [ ] Countdown si es próxima

**Componentes a crear**:
```
src/app/features/voter/
└── election-detail/
    ├── election-detail.component.ts
    ├── election-detail.component.html
    └── election-detail.component.scss
```

**API Endpoints**:
- `GET /api/v1/elections/{id}`

---

#### 4. Proceso de Votación (`/voter/vote/:id`)
**Prioridad**: CRÍTICA
**Descripción**: Interfaz para emitir el voto

**Funcionalidades**:
- [ ] Mostrar opciones de votación (radio buttons o cards)
- [ ] Validación: debe seleccionar una opción
- [ ] Modal de confirmación: "¿Estás seguro?"
- [ ] Envío del voto al backend
- [ ] Generación y descarga del recibo digital (PDF/JSON)
- [ ] Pantalla de confirmación con número de recibo
- [ ] Instrucciones para verificar el voto después

**Componentes a crear**:
```
src/app/features/voter/
├── vote/
│   ├── vote.component.ts
│   ├── vote.component.html
│   └── vote.component.scss
├── vote-confirmation/
│   ├── vote-confirmation.component.ts
│   ├── vote-confirmation.component.html
│   └── vote-confirmation.component.scss
└── components/
    ├── option-card/            # Card de opción de voto
    └── receipt-display/        # Visualización del recibo
```

**API Endpoints**:
- `POST /api/v1/elections/{id}/vote` - Emitir voto
- Respuesta incluye recibo digital

**Flujo**:
1. Seleccionar opción
2. Click "Confirmar voto"
3. Modal: "¿Confirmas tu voto por [opción]?"
4. POST al backend
5. Guardar recibo en localStorage
6. Mostrar pantalla de confirmación
7. Opción de descargar recibo

---

#### 5. Mis Votos (`/voter/my-votes`)
**Prioridad**: MEDIA
**Descripción**: Historial de votos emitidos

**Funcionalidades**:
- [ ] Lista de elecciones en las que ha votado
- [ ] Fecha y hora del voto
- [ ] Ver recibo digital guardado
- [ ] Descargar recibo
- [ ] Verificar que el voto está en la blockchain (futuro)

**Componentes a crear**:
```
src/app/features/voter/
└── my-votes/
    ├── my-votes.component.ts
    ├── my-votes.component.html
    └── my-votes.component.scss
```

**Datos**: 
- Recibos guardados en localStorage
- Verificación contra API (futuro)

---

### 👨‍💼 ADMIN - Administrador (Rol: ADMIN)

#### 6. Dashboard Admin (`/admin/dashboard`)
**Prioridad**: ALTA
**Descripción**: Panel de control para administradores

**Funcionalidades**:
- [ ] Estadísticas generales:
  - Total de elecciones (activas, próximas, cerradas)
  - Total de usuarios registrados
  - Participación general (%)
  - Gráficos de participación por elección
- [ ] Accesos rápidos:
  - Crear nueva elección
  - Ver todas las elecciones
  - Gestionar usuarios (futuro)
- [ ] Alertas:
  - Elecciones que terminan pronto
  - Elecciones con baja participación

**Componentes a crear**:
```
src/app/features/admin/
├── dashboard/
│   ├── dashboard.component.ts
│   ├── dashboard.component.html
│   └── dashboard.component.scss
└── components/
    ├── stats-card/             # Card de estadística
    └── participation-chart/    # Gráfico de participación
```

**API Endpoints**:
- `GET /api/v1/elections/stats` (crear endpoint)
- `GET /api/v1/elections`

---

#### 7. Gestión de Elecciones (`/admin/elections`)
**Prioridad**: ALTA
**Descripción**: Lista completa de elecciones con acciones de gestión

**Funcionalidades**:
- [ ] Tabla con todas las elecciones:
  - ID, Título, Estado, Fecha inicio, Fecha fin
  - Participación (votos/total usuarios)
  - Acciones: Ver, Editar, Cerrar, Resultados
- [ ] Filtros por estado
- [ ] Búsqueda
- [ ] Botón "Nueva Elección"
- [ ] Acciones masivas (futuro)

**Componentes a crear**:
```
src/app/features/admin/
├── elections/
│   ├── election-list/
│   │   ├── election-list.component.ts
│   │   ├── election-list.component.html
│   │   └── election-list.component.scss
│   └── election-form/
│       ├── election-form.component.ts
│       ├── election-form.component.html
│       └── election-form.component.scss
```

**API Endpoints**:
- `GET /api/v1/elections`
- `DELETE /api/v1/elections/{id}` (crear)

---

#### 8. Crear/Editar Elección (`/admin/election/new`, `/admin/election/edit/:id`)
**Prioridad**: CRÍTICA
**Descripción**: Formulario para crear o modificar elecciones

**Funcionalidades**:
- [ ] Formulario con validación:
  - Título (required)
  - Descripción (required)
  - Fecha inicio (required, debe ser futura)
  - Fecha fin (required, debe ser > fecha inicio)
  - Tipo de votación (futuro: simple, múltiple, ranking)
- [ ] Gestión de opciones/candidatos:
  - Añadir opción (nombre, descripción, imagen opcional)
  - Editar opción
  - Eliminar opción
  - Reordenar opciones (drag & drop)
  - Mínimo 2 opciones
- [ ] Previsualización de la elección
- [ ] Botón "Guardar como borrador" (futuro)
- [ ] Botón "Publicar elección"

**Componentes a crear**:
```
src/app/features/admin/
└── election-form/
    ├── election-form.component.ts
    ├── election-form.component.html
    ├── election-form.component.scss
    └── components/
        ├── option-editor/      # Editor de opción
        └── date-picker/        # Selector de fechas
```

**API Endpoints**:
- `POST /api/v1/elections` - Crear
- `PUT /api/v1/elections/{id}` - Actualizar (crear)
- `GET /api/v1/elections/{id}` - Obtener para editar

---

#### 9. Cerrar Elección (`/admin/election/:id/close`)
**Prioridad**: MEDIA
**Descripción**: Página para cerrar una elección manualmente

**Funcionalidades**:
- [ ] Información de la elección
- [ ] Estadísticas actuales de participación
- [ ] Confirmación con contraseña
- [ ] Razón del cierre (opcional)
- [ ] Botón "Cerrar Elección"
- [ ] Notificación de éxito
- [ ] Redirección a resultados

**Componentes a crear**:
```
src/app/features/admin/
└── election-close/
    ├── election-close.component.ts
    ├── election-close.component.html
    └── election-close.component.scss
```

**API Endpoints**:
- `POST /api/v1/elections/{id}/close`

---

#### 10. Resultados de Elección (Admin) (`/admin/election/:id/results`)
**Prioridad**: ALTA
**Descripción**: Ver resultados detallados con estadísticas completas

**Funcionalidades**:
- [ ] Gráfico de resultados (barras, pie chart)
- [ ] Tabla con votos por opción:
  - Opción, Votos, Porcentaje
  - Ordenado por votos descendente
- [ ] Estadísticas:
  - Total de votos emitidos
  - Participación (%)
  - Ganador
  - Fecha de cierre
- [ ] Exportar resultados (CSV, PDF)
- [ ] Timeline de votación (votos por hora/día)
- [ ] Botón "Publicar resultados" (hacer visibles para votantes)

**Componentes a crear**:
```
src/app/features/admin/
└── election-results/
    ├── election-results.component.ts
    ├── election-results.component.html
    ├── election-results.component.scss
    └── components/
        ├── results-chart/      # Gráficos
        └── results-table/      # Tabla de resultados
```

**API Endpoints**:
- `GET /api/v1/elections/{id}/results`

---

### 🔍 AUDITOR (Rol: AUDITOR)

#### 11. Dashboard Auditor (`/auditor/dashboard`)
**Prioridad**: MEDIA
**Descripción**: Panel de auditoría con todas las elecciones

**Funcionalidades**:
- [ ] Lista de todas las elecciones (activas y cerradas)
- [ ] Filtros por estado
- [ ] Indicadores de integridad:
  - ✅ Votos verificados en blockchain
  - ⚠️ Discrepancias detectadas
  - 🔄 En proceso de verificación
- [ ] Acceso a resultados y logs de auditoría

**Componentes a crear**:
```
src/app/features/auditor/
├── dashboard/
│   ├── dashboard.component.ts
│   ├── dashboard.component.html
│   └── dashboard.component.scss
└── components/
    └── integrity-badge/        # Badge de integridad
```

**API Endpoints**:
- `GET /api/v1/elections`
- `GET /api/v1/elections/{id}/audit` (crear)

---

#### 12. Auditoría de Elección (`/auditor/election/:id`)
**Prioridad**: ALTA
**Descripción**: Página de auditoría detallada de una elección

**Funcionalidades**:
- [ ] Información de la elección
- [ ] Resultados actuales/finales
- [ ] Verificación de blockchain:
  - Número de votos en blockchain
  - Número de votos en base de datos
  - Hash de verificación
  - Estado de sincronización
- [ ] Log de eventos:
  - Votos emitidos (timestamp, hash)
  - Cambios de estado
  - Acciones administrativas
- [ ] Exportar informe de auditoría (PDF)
- [ ] Verificar voto individual (por recibo)

**Componentes a crear**:
```
src/app/features/auditor/
├── election-audit/
│   ├── election-audit.component.ts
│   ├── election-audit.component.html
│   └── election-audit.component.scss
└── components/
    ├── blockchain-status/      # Estado de blockchain
    ├── audit-log/              # Log de auditoría
    └── vote-verifier/          # Verificador de voto individual
```

**API Endpoints**:
- `GET /api/v1/elections/{id}/results`
- `GET /api/v1/elections/{id}/audit` (crear)
- `POST /api/v1/elections/{id}/verify-vote` (crear)

---

#### 13. Verificar Voto (`/auditor/verify-vote`)
**Prioridad**: BAJA
**Descripción**: Herramienta para verificar votos individuales

**Funcionalidades**:
- [ ] Input: Número de recibo o hash
- [ ] Buscar voto en blockchain
- [ ] Mostrar información (sin revelar el voto):
  - Timestamp
  - Elección
  - Estado: Válido / Inválido
  - Hash de transacción blockchain
- [ ] Verificar integridad del recibo

**Componentes a crear**:
```
src/app/features/auditor/
└── verify-vote/
    ├── verify-vote.component.ts
    ├── verify-vote.component.html
    └── verify-vote.component.scss
```

**API Endpoints**:
- `POST /api/v1/verify-receipt` (crear)

---

## 🧩 Componentes Compartidos

### Layout Components

#### 14. Main Layout (`/layouts/main-layout`)
**Prioridad**: ALTA
**Descripción**: Layout principal con header, sidebar y content

**Funcionalidades**:
- [ ] Header con:
  - Logo
  - Usuario actual (nombre, rol)
  - Dropdown: Perfil, Cerrar sesión
- [ ] Sidebar con navegación según rol:
  - VOTER: Dashboard, Mis Votos
  - ADMIN: Dashboard, Elecciones, Crear Elección
  - AUDITOR: Dashboard, Verificar Voto
- [ ] Content area con breadcrumbs
- [ ] Responsive (collapse sidebar en móvil)

**Componentes**:
```
src/app/core/layout/
├── main-layout/
│   ├── main-layout.component.ts
│   ├── main-layout.component.html
│   └── main-layout.component.scss
├── header/
│   ├── header.component.ts
│   ├── header.component.html
│   └── header.component.scss
└── sidebar/
    ├── sidebar.component.ts
    ├── sidebar.component.html
    └── sidebar.component.scss
```

---

### Shared Components

#### 15. Componentes Reutilizables
**Prioridad**: MEDIA

**Loading Spinner**:
```typescript
src/app/shared/components/loading-spinner/
```
- Spinner global con overlay

**Error Message**:
```typescript
src/app/shared/components/error-message/
```
- Mostrar errores de API

**Confirmation Dialog**:
```typescript
src/app/shared/components/confirmation-dialog/
```
- Modal de confirmación reutilizable

**Empty State**:
```typescript
src/app/shared/components/empty-state/
```
- Mensaje cuando no hay datos

---

## 🛡️ Guards y Servicios

### Auth Guards

#### 16. Role-based Guards
**Prioridad**: CRÍTICA

**Componentes**:
```
src/app/core/guards/
├── auth.guard.ts              # Requiere autenticación
├── voter.guard.ts             # Solo VOTER
├── admin.guard.ts             # Solo ADMIN
└── auditor.guard.ts           # Solo AUDITOR
```

**Implementación**:
```typescript
// Ejemplo: admin.guard.ts
canActivate(): boolean {
  const user = this.authService.getCurrentUser();
  if (user && user.role === 'ADMIN') {
    return true;
  }
  this.router.navigate(['/unauthorized']);
  return false;
}
```

---

### HTTP Interceptors

#### 17. JWT Interceptor
**Prioridad**: CRÍTICA
**Estado**: ⚠️ POR IMPLEMENTAR

**Funcionalidades**:
- [ ] Añadir token JWT a todas las peticiones HTTP
- [ ] Header: `Authorization: Bearer <token>`
- [ ] Excluir endpoint `/auth/login`

**Archivo**:
```
src/app/core/interceptors/jwt.interceptor.ts
```

---

#### 18. Error Interceptor
**Prioridad**: ALTA
**Estado**: ⚠️ POR IMPLEMENTAR

**Funcionalidades**:
- [ ] Interceptar errores HTTP
- [ ] 401: Redirigir a login (token expirado)
- [ ] 403: Mostrar mensaje de permisos
- [ ] 500: Mostrar error genérico
- [ ] Logging de errores

**Archivo**:
```
src/app/core/interceptors/error.interceptor.ts
```

---

## 🎨 UI/UX - PrimeNG Components

### Componentes PrimeNG a Utilizar

**Data Display**:
- `p-table` - Tablas de elecciones, resultados
- `p-card` - Cards de elecciones
- `p-badge` - Estados, notificaciones
- `p-chip` - Tags, roles
- `p-tag` - Estados de elección

**Form Components**:
- `p-inputText` - Inputs de texto
- `p-calendar` - Selector de fechas
- `p-dropdown` - Selectores
- `p-radioButton` - Opciones de voto
- `p-checkbox` - Checkboxes
- `p-button` - Botones

**Panels**:
- `p-dialog` - Modals de confirmación
- `p-sidebar` - Sidebar de navegación
- `p-panel` - Paneles colapsables
- `p-accordion` - Acordeones

**Data Visualization**:
- `p-chart` - Gráficos (Chart.js)
- `p-progressBar` - Barras de progreso

**Feedback**:
- `p-toast` - Notificaciones toast
- `p-confirmDialog` - Diálogos de confirmación
- `p-message` - Mensajes inline

---

## 📐 Routing Structure

```typescript
const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  
  // VOTER routes
  {
    path: 'voter',
    canActivate: [AuthGuard, VoterGuard],
    children: [
      { path: 'dashboard', component: VoterDashboardComponent },
      { path: 'election/:id', component: ElectionDetailComponent },
      { path: 'vote/:id', component: VoteComponent },
      { path: 'my-votes', component: MyVotesComponent }
    ]
  },
  
  // ADMIN routes
  {
    path: 'admin',
    canActivate: [AuthGuard, AdminGuard],
    children: [
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'elections', component: ElectionListComponent },
      { path: 'election/new', component: ElectionFormComponent },
      { path: 'election/edit/:id', component: ElectionFormComponent },
      { path: 'election/:id/close', component: ElectionCloseComponent },
      { path: 'election/:id/results', component: ElectionResultsComponent }
    ]
  },
  
  // AUDITOR routes
  {
    path: 'auditor',
    canActivate: [AuthGuard, AuditorGuard],
    children: [
      { path: 'dashboard', component: AuditorDashboardComponent },
      { path: 'election/:id', component: ElectionAuditComponent },
      { path: 'verify-vote', component: VerifyVoteComponent }
    ]
  },
  
  { path: 'unauthorized', component: UnauthorizedComponent },
  { path: '**', redirectTo: '/login' }
];
```

---

## 📊 State Management (NGXS)

### States a Crear

#### Auth State
```typescript
src/app/core/state/auth/
├── auth.state.ts
├── auth.actions.ts
└── auth.selectors.ts
```

**Propiedades**:
- `user: User | null`
- `token: string | null`
- `isAuthenticated: boolean`

**Actions**:
- `Login`, `Logout`, `LoadUser`

---

#### Elections State
```typescript
src/app/core/state/elections/
├── elections.state.ts
├── elections.actions.ts
└── elections.selectors.ts
```

**Propiedades**:
- `elections: Election[]`
- `selectedElection: Election | null`
- `loading: boolean`
- `error: string | null`

**Actions**:
- `LoadElections`, `LoadElection`, `CreateElection`, `UpdateElection`, `DeleteElection`, `CloseElection`

---

#### Votes State
```typescript
src/app/core/state/votes/
├── votes.state.ts
├── votes.actions.ts
└── votes.selectors.ts
```

**Propiedades**:
- `myVotes: Vote[]`
- `receipts: Receipt[]`
- `loading: boolean`

**Actions**:
- `SubmitVote`, `LoadMyVotes`, `SaveReceipt`

---

## 🎯 Orden de Implementación Recomendado

### Sprint 1: Fundamentos (2 semanas)
1. ✅ Login page (completado)
2. JWT Interceptor
3. Error Interceptor
4. Auth Guards (role-based)
5. Main Layout + Header + Sidebar
6. Loading Spinner
7. Toast notifications

### Sprint 2: Votante Básico (2 semanas)
8. NGXS Auth State
9. NGXS Elections State
10. Voter Dashboard (lista de elecciones)
11. Election Detail (ver elección)
12. Vote Component (emitir voto)
13. Vote Confirmation

### Sprint 3: Votante Completo (1 semana)
14. NGXS Votes State
15. My Votes (historial)
16. Receipt display y descarga
17. Filtros y búsqueda en dashboard

### Sprint 4: Admin Básico (2 semanas)
18. Admin Dashboard
19. Election List (gestión)
20. Election Form (crear/editar)
21. Validation y error handling

### Sprint 5: Admin Completo (1 semana)
22. Close Election
23. Admin Results View
24. Exportar resultados
25. Estadísticas y gráficos

### Sprint 6: Auditor (1 semana)
26. Auditor Dashboard
27. Election Audit View
28. Blockchain verification display
29. Audit logs

### Sprint 7: Pulido y Testing (1 semana)
30. Responsive design
31. Accesibilidad (a11y)
32. Testing E2E con Playwright
33. Performance optimization

---

## 🧪 Testing Strategy

### Unit Tests
- Componentes: 80% coverage mínimo
- Servicios: 90% coverage mínimo
- Guards: 100% coverage
- Interceptors: 100% coverage

### E2E Tests (Playwright)
- **Voter flow**: Login → Ver elecciones → Votar → Verificar recibo
- **Admin flow**: Login → Crear elección → Gestionar → Ver resultados
- **Auditor flow**: Login → Auditar elección → Verificar votos

---

## 📱 Responsive Design

### Breakpoints
- Mobile: < 768px
- Tablet: 768px - 1024px
- Desktop: > 1024px

### Consideraciones
- Sidebar colapsable en móvil
- Tablas con scroll horizontal
- Forms en una sola columna en móvil
- Gráficos responsive
- Touch-friendly (botones grandes)

---

## 🎨 Theme y Estilos

### PrimeNG Theme
```bash
# Ya configurado en angular.json
"node_modules/primeng/resources/themes/lara-light-blue/theme.css"
```

### Variables CSS Personalizadas
```scss
// src/styles.scss
:root {
  --primary-color: #2563eb;      // Azul
  --success-color: #10b981;      // Verde
  --warning-color: #f59e0b;      // Amarillo
  --danger-color: #ef4444;       // Rojo
  --text-color: #1f2937;         // Gris oscuro
  --bg-color: #f9fafb;           // Gris claro
}
```

---

## 📦 Dependencias Adicionales

```bash
# Ya instaladas
npm install primeng@17 primeicons primeflex
npm install @ngxs/store@18
npm install chart.js

# Por instalar (según necesidad)
npm install file-saver          # Descargar archivos
npm install @types/file-saver
npm install jspdf               # Generar PDFs
npm install html2canvas         # Screenshots para PDF
```

---

## 🚀 Comandos Útiles

```bash
# Generar componente
ng g c features/voter/dashboard

# Generar servicio
ng g s core/services/election

# Generar guard
ng g g core/guards/admin

# Generar interceptor
ng g interceptor core/interceptors/jwt

# Ejecutar tests
npm run test

# Ejecutar E2E
npm run e2e

# Build producción
npm run build
```

---

## 📝 Notas Finales

### Prioridades
1. **CRÍTICO**: Login, Auth, Voting flow
2. **ALTO**: Dashboards, Election management
3. **MEDIO**: Auditoría, Estadísticas
4. **BAJO**: Features avanzadas (exportar, gráficos complejos)

### Consideraciones de Seguridad
- Validar permisos en frontend Y backend
- No exponer información sensible en localStorage
- Sanitizar inputs de usuario
- CORS configurado correctamente
- Token expiration handling

### Mejoras Futuras (Post-MVP)
- Notificaciones push
- Chat soporte
- Verificación en tiempo real blockchain
- Múltiples idiomas (i18n)
- Dark mode
- PWA para móvil

---

**Última actualización**: 17 de noviembre de 2025
**Estado del proyecto**: 🟡 En desarrollo (Login completado, resto por implementar)
