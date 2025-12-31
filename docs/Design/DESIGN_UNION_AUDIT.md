# Perspectiva Sindical: Auditoría, Control y Valor para el Afiliado

Este documento detalla cómo los sindicatos ("Unions") pueden utilizar la infraestructura blockchain para auditar el proceso electoral en tiempo real y cómo pueden comunicar los beneficios de seguridad y transparencia a sus afiliados.

## 1. El Cambio de Paradigma: De Observador a Co-Propietario

En los sistemas de votación electrónica tradicionales, el sindicato actúa como un observador pasivo que debe confiar en los informes generados por el software de la empresa (caja negra).

Con **Hyperledger Fabric**, el sindicato se convierte en **co-propietario de la infraestructura**. Al operar su propio nodo (Peer) y formar parte de las políticas de aval (Endorsement Policies), el sindicato tiene capacidad técnica de veto y verificación independiente.

## 2. Herramientas de Auditoría en Tiempo Real

Los sindicatos disponen de mecanismos técnicos para verificar la "verdad" directamente desde la fuente, sin intermediarios.

### A. Hyperledger Explorer (Panel de Control)
El sistema incluye una instancia de **Hyperledger Explorer** conectada al nodo del sindicato.
*   **Visibilidad:** Permite ver en tiempo real la creación de bloques y el flujo de transacciones.
*   **Datos Observables:**
    *   Número total de votos emitidos (Transacciones).
    *   Timestamp exacto de cada voto.
    *   Identidad del firmante (Hash del certificado).
    *   Estado de la red y salud de los nodos.
*   **Uso Práctico:** Pantalla de monitorización en la sede sindical o acceso web para delegados.

### B. Validación Automática por el Nodo (Peer)
El nodo del sindicato (`peer0.org1` o `peer0.org2`) ejecuta el Chaincode de forma independiente.
*   **Seguridad Activa:** Si la empresa intentara inyectar un voto ilegítimo (ej. usuario duplicado, elección cerrada), el nodo del sindicato **rechazaría la transacción** automáticamente durante la fase de validación.
*   **Alertas:** Los técnicos del sindicato pueden monitorizar los logs del peer para detectar intentos de fraude o errores del sistema.

### C. Consulta Directa al Ledger
Los auditores técnicos del sindicato pueden realizar consultas directas a su copia local de la base de datos (CouchDB) para obtener estadísticas instantáneas sin depender de la API de la empresa.

## 3. Argumentario para los Afiliados ("Venta" del Sistema)

Claves para comunicar los beneficios del sistema a los trabajadores:

### "Nosotros tenemos las llaves" (Soberanía)
> *"La empresa pone los terminales, pero nosotros custodiamos la urna digital. Nada entra en la urna sin que nuestro servidor lo apruebe digitalmente."*

### "El fraude es matemáticamente imposible" (Integridad)
> *"El sistema utiliza criptografía avanzada (Hashes SHA-256). Si alguien intentara alterar un voto, las 'huellas digitales' no coincidirían y el sistema se bloquearía automáticamente. No es cuestión de confianza, es matemáticas."*

### "Tu voto es secreto, tu participación es pública" (Privacidad)
> *"Nadie, ni la empresa ni el sindicato, puede saber qué has votado hasta el recuento conjunto. Sin embargo, tú tienes un recibo que demuestra que tu sobre está dentro de la urna."*

## 4. Demostración de Beneficios (Casos de Uso)

### El "Recibo Antifraude"
El sindicato puede ofrecer un servicio independiente de verificación:
1.  El afiliado recibe un código de verificación (TxID) al votar.
2.  El afiliado introduce ese código en el portal del sindicato.
3.  El portal consulta al **nodo del sindicato** y confirma: *"Tu voto está seguro en el bloque #45 desde las 10:00 AM"*.
4.  **Valor:** Empoderamiento del votante frente a la empresa.

### Pantallas de Transparencia
Durante la jornada electoral, se pueden proyectar dashboards públicos mostrando:
*   Gráficas de participación en tiempo real.
*   Hash del último bloque generado.
*   Estado de consenso entre Sindicato A, Sindicato B y Empresa.

## 5. Resumen de Capacidades

| Capacidad | Sistema Tradicional | Sistema Blockchain (VotacionBC) |
| :--- | :--- | :--- |
| **Rol del Sindicato** | Auditor a posteriori | Validador en tiempo real |
| **Custodia de Votos** | Base de datos de la Empresa | Ledger distribuido (Copia propia) |
| **Verificación** | Fe en la empresa | Verificación criptográfica |
| **Resiliencia** | Punto único de fallo | Red distribuida (Alta disponibilidad) |
