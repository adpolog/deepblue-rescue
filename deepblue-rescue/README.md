# DeepBlue Rescue
### Estudiantes: Andry Polo, Daniel Ramos
### URL Github: https://github.com/adpolog/deepblue-rescue.git
### Branch: feature/repository-layer

## Descripción Breve
**DeepBlue Rescue** es un sistema de gestión backend desarrollado en **Java 21** y **Spring Boot ** diseñado para administrar una red de rescate de fauna marina. Permite el control integral de centros de operación, casos de rescate, expedientes médicos de animales, registro de especialistas con sus respectivas áreas de experticia y la gestión de tratamientos clínicos y de rehabilitación.

---

## Modelo de Datos
El sistema se estructura sobre una base de datos relacional **PostgreSQL**, utilizando claves primarias autoincrementales :

1. **`rescue_centers`**: Almacena la información de los centros de rescate (código único, nombre y ciudad).
2. **`rescue_cases`**: Registra cada caso de rescate asociado a un centro específico, incluyendo fecha, ubicación y estado (`RescueStatus`: ADMITTED, UNDER_EVALUATION, IN_REHABILITATION, READY_FOR_RELEASE, RELEASED, CLOSED).
3. **`animals`**: Contiene los datos taxonómicos e individuales de la fauna rescatada (código, nombre común, nombre científico, sexo y código opcional de dispositivo GPS `tracking_device_code`).
4. **`medical_records`**: Almacena el expediente clínico inicial del animal (peso inicial, condición, lesiones y observaciones).
5. **`specialists`**: Administra el personal profesional capacitado (código profesional, nombre, apellido, correo electrónico y estado activo).
6. **`expertise`**: Catálogo de especialidades o áreas de conocimiento.
7. **`specialist_expertise`**: Tabla intermedia para la relación de muchos a muchos entre especialistas y experticias.
8. **`treatments`**: Registra las intervenciones médicas o de rehabilitación aplicadas a los animales por los especialistas, con fecha/hora, tipo (`TreatmentType`) y descripción.

---

## Relaciones
* **`RescueCenter` 1 : N `RescueCase`**: Un centro de rescate puede gestionar múltiples casos de rescate.
* **`RescueCase` 1 : 1 `Animal`**: Cada caso de rescate está vinculado a un único animal (y viceversa).
* **`Animal` 1 : 1 `MedicalRecord`**: Cada animal posee un único expediente médico inicial.
* **`Animal` 1 : N `Treatment`**: Un animal puede recibir múltiples tratamientos a lo largo de su rehabilitación.
* **`Specialist` 1 : N `Treatment`**: Un especialista puede registrar múltiples tratamientos aplicados.
* **`Specialist` N : M `Expertise`**: Los especialistas pueden tener múltiples áreas de experticia, y una experticia puede pertenecer a varios especialistas.

---

## Explicación de Flyway
**Flyway** es la herramienta de control de versiones para la base de datos utilizada en el proyecto. Permite gestionar y aplicar migraciones de esquemas SQL de forma automatizada, ordenada y repetible en cualquier entorno. En este proyecto se estructuró mediante:
* `V1__create_schema.sql`: Creación inicial de tablas, restricciones de integridad, llaves foráneas e índices.
* `V2__insert_expertise_catalog.sql`: Inserción inicial del catálogo de experticias.
* `V3__add_tracking_device_to_animal.sql`: Modificación incremental para añadir la columna de rastreo GPS en los animales.

---

## Explicación de Testcontainers
**Testcontainers** es una librería de Java que proporciona instancias livianas y desechables de contenedores reales (**PostgreSQL**) mediante Docker para las pruebas de integración. Su uso evita depender de bases de datos en memoria (como H2) que no replican con exactitud el comportamiento, dialecto o restricciones de producción.

---

## Listado de Query Methods Implementados
* **`RescueCenterRepository`**:
    * `findByCode(String code)` — Busca un centro por su código único.
* **`RescueCaseRepository`**:
    * `findByCaseCode(String caseCode)` — Busca un caso por su código único.
    * `findByStatusOrderByRescueDateAsc(RescueStatus status)` — Filtra casos por estado ordenados por fecha ascendente.
    * `findByRescueCenterCode(String centerCode)` — Obtiene los casos asociados a un centro mediante el código del mismo.
    * `findByRescueDateAfterOrderByRescueDateDesc(LocalDate date)` — Busca casos posteriores a una fecha dada, ordenados de forma descendente.
* **`AnimalRepository`**:
    * `findByAnimalCode(String animalCode)` — Busca un animal por su código único.
    * `findByCommonNameContainingIgnoreCase(String commonName)` — Búsqueda por coincidencia parcial en el nombre común ignorando mayúsculas/minúsculas.
    * `findByRescueCaseStatus(RescueStatus status)` — Navega la relación hacia el caso para filtrar por estado.
    * `findByRescueCaseRescueCenterCode(String centerCode)` — Navega múltiples relaciones para filtrar por el código del centro.
* **`ExpertiseRepository`**:
    * `findByNameIgnoreCase(String name)` — Busca una experticia por su nombre ignorando mayúsculas/minúsculas.
* **`SpecialistRepository`**:
    * `findByProfessionalCode(String professionalCode)` — Busca un especialista por su código profesional.
* **`TreatmentRepository`**:
    * `findByAnimalIdOrderByPerformedAtAsc(Long animalId)` — Obtiene los tratamientos de un animal ordenados cronológicamente.

---

## Listado de Consultas JPQL Implementadas
* **`SpecialistRepository`**:
    * `findActiveByExpertise(String expertiseName)`: Selecciona especialistas activos que poseen una experticia específica, navegando la relación N:M y ordenados por apellido.
* **`TreatmentRepository`**:
    * `findTreatmentsBetweenDates(LocalDateTime start, LocalDateTime end)`: Filtra tratamientos ejecutados en un intervalo de tiempo específico (`BETWEEN :start AND :end`).
    * `findByRescueCenterCode(String centerCode)`: Navega desde el tratamiento hasta el centro de rescate (`Treatment -> Animal -> RescueCase -> RescueCenter`).
    * `findBySpecialistExpertiseName(String expertiseName)`: Filtra tratamientos realizados por especialistas que poseen una determinada área de experticia.

---

## Instrucciones para Ejecutar
1. Asegúrate de tener instalado **Java 21** y **Maven**.
2. Configura las credenciales de tu base de datos local en el archivo **`src/main/resources/application.yml`**