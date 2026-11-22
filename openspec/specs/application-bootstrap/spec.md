# application-bootstrap Specification

## Purpose

Define el arranque local de la aplicación sin servicios externos, la persistencia en archivo entre reinicios y la provisión de configuración y secretos desde el entorno, de modo que cualquier desarrollador pueda ejecutar el proyecto con una preparación mínima.

## Requirements

### Requirement: Arranque sin infraestructura externa
El backend SHALL iniciarse y quedar listo para atender peticiones sin requerir Docker, servidores de base de datos externos ni otros servicios instalados previamente.

#### Scenario: Primer arranque en una máquina limpia
- **WHEN** se inicia el backend por primera vez con la configuración por defecto del repositorio
- **THEN** el proceso arranca correctamente y crea el archivo de base de datos H2 en `./data/turnosdb`

#### Scenario: Reinicio con datos existentes
- **WHEN** se reinicia el backend después de haberse detenido
- **THEN** reutiliza el archivo de base de datos existente y conserva los datos persistidos

### Requirement: Consola H2 disponible sólo en desarrollo
El entorno de desarrollo SHALL exponer la consola H2; los entornos que no sean de desarrollo SHALL NOT exponerla.

#### Scenario: Entorno de desarrollo
- **WHEN** el backend se ejecuta con el perfil `dev`
- **THEN** la consola H2 está disponible en `/h2-console`

#### Scenario: Entorno de prueba
- **WHEN** el backend se ejecuta con el perfil `test`
- **THEN** la consola H2 no está habilitada

### Requirement: Configuración sensible externalizada
La configuración SHALL obtenerse de variables de entorno con valores por defecto seguros, y el repositorio SHALL NOT contener secretos reales ni datos personales.

#### Scenario: Provisión de un secreto
- **WHEN** se define `JWT_SECRET` en el entorno antes de iniciar el backend
- **THEN** la aplicación utiliza ese valor y no registra su contenido en los logs

#### Scenario: Ausencia de secretos en el repositorio
- **WHEN** se inspeccionan los archivos versionados de configuración
- **THEN** todos los valores sensibles aparecen como placeholders o referencias a variables de entorno, nunca como valores reales

### Requirement: Estructura de proyectos ejecutables
El repositorio SHALL contener proyectos backend y frontend independientes, y el frontend SHALL compilar con tipado estricto.

#### Scenario: Proyectos presentes
- **WHEN** se clona el repositorio
- **THEN** existen `backend/pom.xml` y `frontend/package.json` junto con sus directorios fuente según la arquitectura propuesta

#### Scenario: Tipado estricto del frontend
- **WHEN** se compila el proyecto frontend
- **THEN** la compilación de TypeScript no presenta errores de tipos y no se utiliza ningún valor `any` implícito o explícito

### Requirement: Esquema migrado y validado al arrancar
El backend SHALL aplicar las migraciones Flyway antes de inicializar el mapa de entidades y SHALL validar que el esquema resultante coincida con las entidades.

#### Scenario: Arranque con migraciones presentes
- **WHEN** se inicia el backend con las migraciones V001 a V004 en el classpath
- **THEN** Flyway las aplica en orden y Hibernate valida el esquema sin errores

#### Scenario: Esquema fuera de sincronía
- **WHEN** una entidad no coincide con el esquema migrado
- **THEN** el arranque falla con un error de validación que identifica la diferencia
