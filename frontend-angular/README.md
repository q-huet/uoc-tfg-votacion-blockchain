# Frontend Angular - Sistema de Votación Sindical

Aplicación frontend para el sistema de votación sindical basado en blockchain.

## 🚀 Tecnologías

- **Angular 17** - Framework principal
- **PrimeNG 17** - Componentes UI
- **NGXS** - Gestión de estado
- **RxJS** - Programación reactiva
- **Chart.js** - Gráficos y visualizaciones

## 📋 Requisitos previos

- Node.js 20.x LTS
- npm 10.x
- Angular CLI 17

## 🛠️ Instalación

```bash
# Instalar dependencias
npm install

# Iniciar servidor de desarrollo
npm start

# Compilar para producción
npm run build
```

## 🏗️ Estructura del proyecto

```
src/
├── app/
│   ├── core/                 # Servicios core y modelos
│   │   ├── services/        # AuthService, ElectionService, etc.
│   │   ├── models/          # Interfaces y tipos
│   │   ├── guards/          # Guards de navegación
│   │   └── interceptors/    # HTTP interceptors
│   ├── shared/              # Componentes compartidos
│   │   └── components/      # Header, Footer, etc.
│   ├── features/            # Módulos de características
│   │   ├── auth/           # Login, registro
│   │   ├── elections/      # Listado de elecciones
│   │   └── voting/         # Proceso de votación
│   └── environments/        # Configuración de entornos
└── assets/                  # Recursos estáticos
```

## 🔗 Conexión con Backend

El frontend se conecta al backend Spring Boot en:
- **Desarrollo:** `http://localhost:8080/api/v1`
- **Producción:** Configurar en `environment.prod.ts`

## 👤 Usuarios de prueba

Los usuarios de prueba están configurados en el backend:
- **Usuario:** `test.user` / **Password:** `password123`
- **Admin:** `admin` / **Password:** `password123`
- **Auditor:** `auditor` / **Password:** `password123`

Ver más usuarios en `backend-spring/src/main/resources/mock/README.md`

## 🎨 Temas y estilos

El proyecto usa el tema **Lara Light Blue** de PrimeNG.

## 📝 Scripts disponibles

- `npm start` - Inicia el servidor de desarrollo
- `npm run build` - Compila para producción  
- `npm test` - Ejecuta las pruebas

## 🔐 Autenticación

El sistema usa JWT (JSON Web Tokens) para la autenticación.

## 🚧 Estado actual

✅ Estructura base creada
✅ Componente de login funcional
✅ Servicio de autenticación implementado
✅ Configuración de rutas básica
⏳ Pendiente: Componentes de elecciones
⏳ Pendiente: Proceso de votación
⏳ Pendiente: Dashboard de resultados

---

**Autor:** Enrique Huet Adrover  
**Universidad:** UOC  
**Año:** 2025
