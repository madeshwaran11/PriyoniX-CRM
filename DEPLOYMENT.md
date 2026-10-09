# Deployment guide

## Database: Supabase PostgreSQL

Supabase provides PostgreSQL, so this project uses the PostgreSQL JDBC driver. It is not compatible with MySQL.

Create a Supabase project, then select **Connect** and copy the **Session pooler** connection details. Render commonly needs this IPv4-compatible connection method. The host and username must be copied exactly from Supabase; do not construct them manually.

## Backend: Render

Create a **Web Service** from this GitHub repository.

- Runtime: Java
- Root directory: leave empty
- Build command: `cd backend && ./mvnw clean package -DskipTests`
- Start command: `java -jar backend/target/crm-0.0.1-SNAPSHOT.jar`

Set these environment variables in Render:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_SUPABASE_POOLER_HOST:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.YOUR_PROJECT_REF
SPRING_DATASOURCE_PASSWORD=YOUR_SUPABASE_DATABASE_PASSWORD
SPRING_MAIL_USERNAME=YOUR_GMAIL_ADDRESS
SPRING_MAIL_PASSWORD=YOUR_NEW_GMAIL_APP_PASSWORD
APP_CORS_ALLOWED_ORIGINS=https://YOUR-VERCEL-PROJECT.vercel.app
```

Copy the Render service URL when deployment completes, e.g. `https://your-api.onrender.com`.

## Frontend: Vercel

Import the same GitHub repository in Vercel.

- Root Directory: `frontend`
- Framework: Vite
- Build Command: `npm run build`
- Output Directory: `dist`

Add this Vercel environment variable before deploying:

```
VITE_API_URL=https://YOUR-RENDER-SERVICE.onrender.com/api
```

After the first Vercel deployment, update `APP_CORS_ALLOWED_ORIGINS` in Render with the exact Vercel production URL and redeploy the Render service.

## Local development

Create `frontend/.env` from `frontend/.env.example`. For the backend, use environment variables or your IDE run configuration; do not put passwords in `application.properties`.
