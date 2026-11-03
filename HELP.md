# Getting Started

### SonarQube

Local analysis uses SonarQube Community Build and a separate PostgreSQL database,
both managed by Docker Compose. It does not use or change the bookstore MySQL
database. Install Docker with Compose and use Java 21 for Maven. Allow at least
4 GB of Docker memory for SonarQube, plus memory for MySQL integration tests.

1. Copy `.env.example` to `.env` and set `SONAR_DB_PASSWORD` to a strong local
   password. Keep this value stable while reusing the database volume.
   `.env` is ignored by Git; never commit database passwords or analysis tokens.
2. Run `docker compose up -d --wait --wait-timeout 600`.
3. Open <http://localhost:9000>, sign in with the initial `admin` / `admin`
   credentials, and change the administrator password when prompted.
4. Create a local project with key `bookstore-backend`. Under **My Account >
   Security**, generate a project analysis token with an expiration date and
   permission to analyze that project.

Run analysis from this directory. Enter the token without putting it in shell
history (these commands work in Bash and Zsh):

```sh
export SONAR_HOST_URL=http://localhost:9000
printf 'SonarQube token: '
read -rs SONAR_TOKEN
printf '\n'
export SONAR_TOKEN
./mvnw clean verify sonar:sonar
unset SONAR_TOKEN
```

`verify` runs unit and integration tests and generates combined JaCoCo coverage
in `target/site/jacoco/jacoco.xml` and an HTML report at
`target/site/jacoco/index.html`. Both Surefire and Failsafe append to the same
coverage data file; use `clean` to avoid stale results. The Maven scanner imports
the XML report and determines source/test paths from Maven. No separate
`sonar-project.properties` is needed. Analysis is explicit; ordinary builds do
not contact SonarQube. View results at
<http://localhost:9000/dashboard?id=bookstore-backend>.

To make the command fail when the server's assigned quality gate fails, run
`./mvnw clean verify sonar:sonar -Dsonar.qualitygate.wait=true`.
No custom gate or coverage threshold is imposed by this repository.

Use `docker compose logs -f sonarqube` to troubleshoot startup and
`docker compose down` to stop the stack. Named volumes retain projects,
configuration, and analysis history. Do not remove the volumes unless you intend
to erase that data. PostgreSQL is not exposed to the host; SonarQube is bound
only to `127.0.0.1:9000`. This is a local development setup, not a production
deployment.

On Linux, Elasticsearch also requires host `vm.max_map_count` of at least
`524288`; configure the host according to SonarQube's requirements rather than
disabling bootstrap checks. On Docker Desktop, this setting applies to its
Linux VM. If port 9000 is occupied, change the host port in `compose.yaml` and
set `SONAR_HOST_URL` accordingly.

### Testing

Tests use JUnit Jupiter, Mockito, and Spring MockMvc. Spring Boot 4.1 manages
JUnit 6, which retains the Jupiter API familiar from JUnit 5; do not override it
with JUnit 5 because Spring's test extension requires JUnit 6. Java 21 is required.

```sh
./mvnw test                         # Unit tests; no Docker or local MySQL required
./mvnw verify                       # Unit and MySQL integration tests; Docker required
./mvnw -Dtest=BookServiceTests test  # One unit test class
./mvnw -Dit.test=BookApiIT verify    # Select an integration test class (also runs unit tests)
```

Surefire runs `*Tests` during `test`; Failsafe runs `*IT` during `verify`.
Reports are written to `target/surefire-reports` and `target/failsafe-reports`.
Integration tests fail rather than silently skip when Docker is unavailable.
The first integration run downloads the `mysql:8.4` image.

Unit tests cover book service behavior, DTO mapping, and controller validation
and HTTP responses with a mocked service. Integration tests load the complete
Spring application and use MockMvc with real services, repositories, and MySQL.
They cover creation, relationship loading, pagination, duplicate ISBNs,
validation, missing resources, and soft deletion with retained order history.

The `test` profile uses a disposable Testcontainers MySQL database initialized
from `database/schema.sql`; Hibernate validates the real schema rather than
generating a test-only substitute. Connection properties are supplied by the
container, never the local development database. Rows are cleared before each
integration test, and the container is removed when the suite finishes.

### Book API

`POST /api/books` creates a book and returns `201 Created`, the book response,
and a `Location` header pointing to `/api/books/{id}`. Example JSON body:

```json
{
  "isbn": "9780134685991",
  "title": "Effective Java",
  "price": 45.00,
  "language": "en",
  "stockQuantity": 10,
  "publisherId": 1,
  "categoryId": 1,
  "authorIds": [1]
}
```

`isbn`, `title`, and `price` are required. Optional fields include `description`,
`pageCount`, `publicationYear`, and `isActive`. Omitting `language`,
`stockQuantity`, or `isActive` defaults them to `"en"`, `0`, and `true`.
Publisher, category, and author IDs must reference existing records; they may
be omitted. Invalid input returns `400`, missing related records return `404`,
and duplicate ISBNs return `409` (including ISBNs belonging to inactive books).

`DELETE /api/books/{id}` marks the book inactive and returns `204 No Content`.
Repeating deletion of an existing inactive book also returns `204`; an unknown
ID returns `404`. The book and its order history are retained. Inactive books
are excluded from `GET /api/books` but remain accessible through
`GET /api/books/{id}`.

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.2/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.2/maven-plugin/build-image.html)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.
