# Frontend - Sistema de Votación (Angular)

Este directorio contiene la interfaz de usuario web, desarrollada en Angular.

## 🛠️ Tecnologías

*   **Angular 17+**: Framework frontend.
*   **Angular Material**: Componentes de UI.
*   **TypeScript**: Lenguaje de programación.

## 📂 Estructura

*   `src/app`: Componentes, servicios y modelos de la aplicación.
*   `angular.json`: Configuración de build de Angular.

## � Seguridad en el Cliente

El frontend juega un papel crucial en la privacidad del votante:

1.  **Cifrado Asimétrico**: Utiliza la librería `crypto-js` (o Web Crypto API) para cifrar la opción de voto seleccionada.
2.  **Clave Pública**: Al cargar una elección, el frontend obtiene la **Clave Pública** asociada a esa elección.
3.  **Envío Seguro**: El payload enviado al backend contiene únicamente el dato cifrado. El texto plano de la intención de voto nunca sale del navegador del usuario.

## �🚀 Ejecución

El frontend se comunica con el backend en `http://localhost:8080`.

### Instalación de dependencias
```bash
npm install
```

### Servidor de desarrollo
```bash
ng serve
```
La aplicación estará disponible en `http://localhost:4200`.

O utilizando el script de utilidad:
```bash
../scripts/run-frontend.sh
```
