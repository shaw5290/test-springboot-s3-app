# Simple Storage App

A Spring Boot application that stores files in an S3-compatible bucket and keeps metadata in MySQL. Upload files (max 5 MB), list them, and delete them. No authentication is implemented.

**Requirements:** Java 21, Maven, MySQL, and an S3-compatible storage (AWS S3 or MinIO).

---

## Quick start (development)

1. **Create the database**
   ```sql
   CREATE DATABASE simple_storage_app;
   ```

2. **Configure environment variables** (or edit `src/main/resources/application.properties`):
   - MySQL: `MYSQL_USERNAME`, `MYSQL_PASSWORD`, `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`
   - S3: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `S3_REGION`, `S3_BUCKET`, `S3_ENDPOINT`

3. **Run the app**
   ```bash
   mvn spring-boot:run
   ```
   Open **http://localhost:8080**

---

## Development

### Prerequisites

- **Java 21**
- **Maven 3.9+**
- **MySQL 8** (or compatible)
- **S3-compatible storage**: AWS S3, or [MinIO](https://min.io/) for local dev

### Configuration (dev)

Edit `src/main/resources/application.properties` or set environment variables (see [Environment variables](#environment-variables) below).

| Property / Env | Default | Description |
|----------------|---------|-------------|
| `MYSQL_HOST` | `localhost` | MySQL host |
| `MYSQL_PORT` | `3306` | MySQL port |
| `MYSQL_DATABASE` | `simple_storage_app` | Database name |
| `MYSQL_USERNAME` | `root` | MySQL user |
| `MYSQL_PASSWORD` | *(empty)* | MySQL password |
| `S3_ENDPOINT` | *(empty)* | S3 endpoint (set for MinIO) |
| `S3_REGION` | `us-east-1` | S3 region |
| `S3_BUCKET` | `simple-storage-app` | Bucket name |
| `AWS_ACCESS_KEY_ID` | *(empty)* | S3 access key |
| `AWS_SECRET_ACCESS_KEY` | *(empty)* | S3 secret key |
| `S3_PATH_STYLE` | `false` | Set `true` for MinIO |

### Development: S3 / MinIO

**Option A – MinIO (local)**

1. Run MinIO (e.g. Docker):
   ```bash
   docker run -p 9000:9000 -p 9001:9001 minio/minio server /data --console-address ":9001"
   ```
2. In MinIO console (http://localhost:9001) create a bucket, e.g. `simple-storage-app`.
3. Set environment variables:
   ```bash
   export S3_ENDPOINT=http://localhost:9000
   export AWS_ACCESS_KEY_ID=minioadmin
   export AWS_SECRET_ACCESS_KEY=minioadmin
   export S3_PATH_STYLE=true
   export S3_BUCKET=simple-storage-app
   ```

**Option B – AWS S3**

- Set `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` (or use IAM/instance profile and leave them empty).
- Ensure `S3_REGION` matches your bucket.
- Leave `S3_ENDPOINT` unset for standard AWS S3.

### Build and run (dev)

```bash
# Compile
mvn compile

# Run (uses application.properties + env)
mvn spring-boot:run
```

The app creates the `stored_files` table on first run (`spring.jpa.hibernate.ddl-auto=update`).

### Environment variables

Spring Boot maps each property to an environment variable: **UPPERCASE** with **dots and hyphens replaced by underscores**. For example `mysql.host` → `MYSQL_HOST`, `app.s3.path-style-access` → `APP_S3_PATH_STYLE_ACCESS`.

| Environment variable | Description |
|------------------------|-------------|
| `MYSQL_HOST` | MySQL host |
| `MYSQL_PORT` | MySQL port |
| `MYSQL_DATABASE` | Database name |
| `MYSQL_USERNAME` | MySQL user |
| `MYSQL_PASSWORD` | MySQL password |
| `S3_ENDPOINT` | S3 endpoint override |
| `S3_REGION` | S3 region |
| `S3_BUCKET` | Bucket name |
| `AWS_ACCESS_KEY_ID` | S3 access key |
| `AWS_SECRET_ACCESS_KEY` | S3 secret key |
| `S3_PATH_STYLE` | Force path-style access |

```powershell
$env:MYSQL_HOST = "localhost"
$env:MYSQL_USERNAME = "root"
$env:MYSQL_PASSWORD = "yourpassword"
$env:S3_ENDPOINT = "http://localhost:9000"
$env:AWS_ACCESS_KEY_ID = "minioadmin"
$env:AWS_SECRET_ACCESS_KEY = "minioadmin"
$env:S3_PATH_STYLE = "true"
$env:S3_BUCKET = "simple-storage-app"
mvn spring-boot:run
```

**Linux / macOS / Git Bash** – current session:

```bash
export MYSQL_HOST=localhost
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=yourpassword
export S3_ENDPOINT=http://localhost:9000
export AWS_ACCESS_KEY_ID=minioadmin
export AWS_SECRET_ACCESS_KEY=minioadmin
export S3_PATH_STYLE=true
export S3_BUCKET=simple-storage-app
mvn spring-boot:run
```

**Linux / macOS** – one-off run (env vars only for that command):

```bash
MYSQL_PASSWORD=secret S3_ENDPOINT=http://localhost:9000 AWS_ACCESS_KEY_ID=minioadmin AWS_SECRET_ACCESS_KEY=minioadmin S3_PATH_STYLE=true mvn spring-boot:run
```

**Production JAR** – set env vars in the same way in your process manager (systemd, Docker, Kubernetes, etc.), then run:

```bash
java -jar target/simple_storage_app-0.0.1-SNAPSHOT.jar
```

Env vars override values in `application.properties`; you only need to set the ones you want to change.

---

## Production

### Build

```bash
mvn clean package -DskipTests
```

JAR: `target/simple_storage_app-0.0.1-SNAPSHOT.jar`

### Run the JAR

```bash
java -jar target/simple_storage_app-0.0.1-SNAPSHOT.jar
```

Prefer **external configuration** instead of baking secrets into the JAR.

### Production configuration

Use one or more of:

1. **Environment variables** (same names as in the table above, where applicable).
2. **External `application.properties`**:
   ```bash
   java -jar target/simple_storage_app-0.0.1-SNAPSHOT.jar --spring.config.location=file:/opt/app/application.properties
   ```
3. **Command-line arguments**:
   ```bash
   java -jar target/simple_storage_app-0.0.1-SNAPSHOT.jar \
     --MYSQL_HOST=db.example.com \
     --MYSQL_USERNAME=appuser \
     --MYSQL_PASSWORD=secret \
     --AWS_ACCESS_KEY_ID=... \
     --AWS_SECRET_ACCESS_KEY=...
   ```

### Production checklist

- **MySQL**: Dedicated DB and user with minimal privileges; strong password; TLS if possible.
- **S3**: Use IAM credentials or instance/profile; restrict bucket policy to this app; enable versioning/backup if needed.
- **Network**: Run the app behind HTTPS (reverse proxy or load balancer).
- **Security**: This app has **no authentication**. In production, put it behind an auth layer (e.g. VPN, OAuth, API gateway, or reverse proxy with auth).

### File limit

Upload limit is **5 MB** (`spring.servlet.multipart.max-file-size=5MB`). Change in `application.properties` or override with:

```bash
--spring.servlet.multipart.max-file-size=10MB
```

---

## Summary

| Environment | Run command | Config |
|-------------|-------------|--------|
| **Dev** | `mvn spring-boot:run` | `application.properties` + optional env |
| **Production** | `java -jar target/simple_storage_app-0.0.1-SNAPSHOT.jar` | Env vars or external `application.properties` / `--key=value` |
