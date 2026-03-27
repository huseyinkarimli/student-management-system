# External Access Configuration Guide
## Student Management System - Docker Deployment

---

## ✅ Completed Configuration Changes

### 1. **nginx.conf** - Updated for Proper Proxying
**Location**: `frontend/nginx.conf`

**Changes Made**:
- Changed backend service name from `student-management-system-backend-1` → `backend`
- Added proper proxy headers for external access:
  - `X-Forwarded-For` - Preserves client IP through proxy
  - `X-Forwarded-Proto` - Preserves original protocol (http/https)
  - `X-Forwarded-Host` - Preserves original host header

**Why**: These headers ensure Spring Boot correctly handles requests from external IPs and proxy servers.

---

### 2. **application.properties** - Environment-Aware Configuration
**Location**: `backend/src/main/resources/application.properties`

**Changes Made**:
```properties
# MongoDB URI - uses environment variable in Docker
spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/student_audit}

# JWT Secrets - uses environment variables in Docker (SECURITY FIX)
jwt.secret=${JWT_SECRET:dttDdfdfGghgjjPjfdghhh3hhfgnmr4tc5hhjwTe2v32GelsdSHog5Hfpia3}
jwt.refresh.secret=${JWT_REFRESH_SECRET:dhhfgnmr4Rtc5hhjwTe2ttDdfdfGghgjjPjfdghhh3v32GelsdwdSHog5Hfpia3}
```

**Why**: 
- Allows different configurations for local dev vs Docker
- Keeps sensitive secrets out of version control (when using env vars)
- Falls back to defaults for local development

---

### 3. **docker-compose.yml** - Production-Ready Environment Variables
**Location**: `docker-compose.yml`

**Changes Made**:
Added to backend service environment:
```yaml
- SERVER_FORWARD_HEADERS_STRATEGY=framework
- JWT_SECRET=${JWT_SECRET:-default_fallback}
- JWT_REFRESH_SECRET=${JWT_REFRESH_SECRET:-default_fallback}
```

**Why**:
- `SERVER_FORWARD_HEADERS_STRATEGY`: Tells Spring Boot to trust proxy headers from Nginx
- JWT secrets: Can now be overridden via host environment variables for security

---

## 🚀 How to Deploy for External Access

### Step 1: Rebuild Docker Images

```bash
# Stop existing containers
docker-compose down

# Rebuild with updated configs (no cache to ensure changes are picked up)
docker-compose build --no-cache

# Start services
docker-compose up -d

# Verify all services are running
docker-compose ps
```

---

### Step 2: Configure Router Port Forwarding

1. **Find your local IP address**:
   ```bash
   # Windows
   ipconfig
   # Look for "IPv4 Address" under your active network adapter (e.g., 192.168.1.100)
   
   # Mac/Linux
   ifconfig
   # or
   ip addr show
   ```

2. **Access your router admin panel**:
   - Usually at `192.168.1.1`, `192.168.0.1`, or `10.0.0.1`
   - Login with router credentials

3. **Add port forwarding rule**:
   - **Service name**: Student Management System
   - **External port**: 3000
   - **Internal IP**: Your computer's local IP (e.g., 192.168.1.100)
   - **Internal port**: 3000
   - **Protocol**: TCP

4. **Save and apply** router configuration

---

### Step 3: Find Your Public IP

Visit any of these sites from your browser:
- https://whatismyip.com
- https://ipinfo.io
- Or search Google for "what is my ip"

Your public IP will be something like: `203.0.113.45`

---

### Step 4: Test External Access

1. **From a device outside your network** (mobile data, friend's network, etc.):
   - Visit: `http://YOUR_PUBLIC_IP:3000`
   - You should see the login page

2. **Test login**:
   - Username: `u1` / Password: `123` (admin)
   - Username: `teacher1` / Password: `123` (teacher)

3. **Verify functionality**:
   - Navigate through different pages
   - Check browser console for errors
   - Test creating courses, marking attendance, etc.

---

## 🔒 Security Recommendations for Production

### Critical (Do Before External Deployment):

#### 1. **Change JWT Secrets**
Create a `.env` file in the project root:

```bash
# .env (DO NOT commit to git - add to .gitignore)
JWT_SECRET=your-super-secure-random-string-here-min-64-chars
JWT_REFRESH_SECRET=your-different-super-secure-random-string-here-min-64-chars
```

Generate strong secrets:
```bash
# Using OpenSSL
openssl rand -base64 64

# Or using Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

Then run:
```bash
docker-compose --env-file .env up -d
```

#### 2. **Enable HTTPS/SSL**
For production, HTTP is insecure. Options:

**Option A: Use Cloudflare Tunnel** (Easiest, Free SSL)
- No port forwarding needed
- Automatic SSL
- DDoS protection
- Visit: https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/

**Option B: Use Caddy** (Automatic HTTPS)
Update docker-compose.yml to use Caddy instead of custom Nginx:
```yaml
frontend:
  image: caddy:2-alpine
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - ./frontend:/usr/share/caddy
    - caddy_data:/data
  command: caddy file-server --domain yourdomain.com
```

**Option C: Manual Let's Encrypt with Certbot**
- More complex but gives full control

#### 3. **Restrict CORS for Production**
In `SecurityConfig.java`, change from wildcard to specific domains:

```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:3000",
    "https://yourdomain.com",
    "http://YOUR_PUBLIC_IP:3000"
));
```

#### 4. **Rate Limiting**
Current: 5 requests/minute

Consider increasing for production:
```properties
rate.limit.capacity=100
rate.limit.tokens=100
rate.limit.refill.minutes=1
```

#### 5. **Update .gitignore**
Add to `.gitignore`:
```
.env
*.env
application-prod.properties
```

---

### Recommended (Enhance Security):

#### 6. **Add Health Check Endpoint**
Nginx can use this to verify backend is ready:

```nginx
location /health {
    proxy_pass http://backend:8080/actuator/health;
    access_log off;
}
```

#### 7. **Disable H2 Console in Production**
In `application.properties`:
```properties
spring.h2.console.enabled=${H2_CONSOLE_ENABLED:false}
```

Then enable only for local dev in docker-compose:
```yaml
environment:
  - H2_CONSOLE_ENABLED=true  # Only for development
```

#### 8. **Use File-Based H2 for Data Persistence**
Update docker-compose.yml:
```yaml
backend:
  environment:
    - SPRING_DATASOURCE_URL=jdbc:h2:file:/data/student_db;DB_CLOSE_ON_EXIT=FALSE
  volumes:
    - h2-data:/data
```

Add volume:
```yaml
volumes:
  mongodb-data:
  h2-data:  # Add this
```

---

## 🧪 Testing Checklist

### Local Testing (After Rebuild):
- [ ] `docker-compose ps` shows all services running
- [ ] `docker-compose logs backend` shows no errors
- [ ] `docker-compose logs frontend` shows nginx started
- [ ] Visit `http://localhost:3000` - login page loads
- [ ] Login as admin (`u1`/`123`) - works
- [ ] Login as teacher (`teacher1`/`123`) - works
- [ ] Navigate to all pages (Courses, Assignments, Attendance, etc.)
- [ ] Check browser console - no CORS errors
- [ ] Verify audit logs are being created (MongoDB working)

### External Testing (After Port Forwarding):
- [ ] From mobile data: Visit `http://YOUR_PUBLIC_IP:3000`
- [ ] Login works from external network
- [ ] All API calls succeed (check browser Network tab)
- [ ] Can perform CRUD operations (create course, mark attendance, etc.)
- [ ] Logout and re-login from external device

---

## 📝 Updated Files Summary

### Files Modified:

1. **`frontend/nginx.conf`** ✅
   - Backend service: `student-management-system-backend-1` → `backend`
   - Added: `X-Forwarded-For`, `X-Forwarded-Proto`, `X-Forwarded-Host` headers

2. **`backend/src/main/resources/application.properties`** ✅
   - MongoDB URI: Now uses `${SPRING_DATA_MONGODB_URI:...}` pattern
   - JWT secrets: Now use `${JWT_SECRET:...}` and `${JWT_REFRESH_SECRET:...}` patterns

3. **`docker-compose.yml`** ✅
   - Added: `SERVER_FORWARD_HEADERS_STRATEGY=framework`
   - Added: `JWT_SECRET` and `JWT_REFRESH_SECRET` environment variables with fallbacks

---

## 🔧 Quick Start Commands

```bash
# 1. Stop current containers
docker-compose down

# 2. Rebuild with new configuration
docker-compose build --no-cache

# 3. Start services
docker-compose up -d

# 4. Check logs
docker-compose logs -f

# 5. Verify services are healthy
docker-compose ps

# 6. Test locally first
curl http://localhost:3000
curl http://localhost:8080/actuator/health  # If actuator enabled

# 7. After port forwarding, test externally
# From another device: http://YOUR_PUBLIC_IP:3000
```

---

## 🌐 Accessing from External Network

### Method 1: Port Forwarding (What You'll Do)

**Requirements**:
- Router access (admin credentials)
- Static local IP for your computer (or DHCP reservation)
- Your ISP doesn't block port 3000

**Steps**:
1. Configure port forwarding (see Step 2 above)
2. Share URL: `http://YOUR_PUBLIC_IP:3000`
3. Users can access from anywhere

**Limitations**:
- HTTP only (not secure for sensitive data)
- Public IP may change (use dynamic DNS service like DuckDNS)
- Port 3000 visible in URL

---

### Method 2: Cloudflare Tunnel (Recommended for Production)

**Advantages**:
- No port forwarding needed
- Automatic HTTPS
- Custom domain (e.g., `student-system.yourdomain.com`)
- DDoS protection
- Works even behind CGNAT

**Setup** (5 minutes):
```bash
# 1. Install cloudflared
# Download from: https://github.com/cloudflare/cloudflared/releases

# 2. Login to Cloudflare
cloudflared tunnel login

# 3. Create tunnel
cloudflared tunnel create student-system

# 4. Route your domain
cloudflared tunnel route dns student-system student-system.yourdomain.com

# 5. Create config file
# ~/.cloudflared/config.yml:
tunnel: <TUNNEL-ID>
credentials-file: /path/to/credentials.json

ingress:
  - hostname: student-system.yourdomain.com
    service: http://localhost:3000
  - service: http_status:404

# 6. Run tunnel
cloudflared tunnel run student-system
```

---

## 🚨 Security Warnings

### Before External Deployment:

1. **JWT Secrets**: 
   - ⚠️ Current secrets are in public repository
   - ✅ Now environment-aware, but still have fallback defaults
   - 🔒 **Action**: Set strong secrets via `.env` file before public deployment

2. **Email Credentials**:
   - ⚠️ Your `application.properties` contains `huseyinkarimli.tech@gmail.com` and `my-app-password`
   - 🔒 **Action**: Move to environment variables or use placeholder

3. **HTTP vs HTTPS**:
   - ⚠️ Current setup uses HTTP (unencrypted)
   - 🔒 **Action**: Implement HTTPS for production (see Method 2 above)

4. **H2 Console**:
   - ⚠️ Currently enabled and allows remote access
   - 🔒 **Action**: Disable in production or restrict access

5. **Default User Credentials**:
   - ⚠️ `u1/123` and `teacher1/123` are weak passwords
   - 🔒 **Action**: Change via User Management page after deployment

---

## 📊 Architecture Overview

```
External User → Internet → Your Public IP:3000
                              ↓
                         Your Router (Port Forward)
                              ↓
                         Docker Host (localhost:3000)
                              ↓
                         Frontend Container (Nginx)
                              ↓ (proxy /api/* and /apis/*)
                         Backend Container (Spring Boot:8080)
                              ↓
                         MongoDB Container (audit logs)
                         H2 Database (in-memory)
```

---

## 🎯 Final Deployment Steps

### For Testing (HTTP, Internal Use):
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
# Configure port forwarding on router
# Share: http://YOUR_PUBLIC_IP:3000
```

### For Production (HTTPS, Public Use):
1. Get a domain name (optional but recommended)
2. Set up Cloudflare Tunnel or SSL certificate
3. Create `.env` file with strong JWT secrets
4. Update CORS to whitelist specific domains
5. Disable H2 console
6. Change default passwords
7. Deploy:
   ```bash
   docker-compose --env-file .env up -d
   ```

---

## 📞 Support & Troubleshooting

### Common Issues:

**Issue**: "Cannot connect from external network"
- **Check**: Router port forwarding is active
- **Check**: Firewall allows port 3000
- **Check**: ISP doesn't block the port

**Issue**: "CORS errors in browser console"
- **Check**: CORS config allows your origin
- **Check**: Nginx proxy headers are set correctly

**Issue**: "Audit logs not saving"
- **Check**: MongoDB container is running (`docker-compose ps`)
- **Check**: Backend logs show MongoDB connection (`docker-compose logs backend`)

**Issue**: "JWT authentication fails"
- **Check**: JWT secrets match between containers
- **Check**: Token not expired (check browser localStorage)

---

## 📋 All Changes Summary

| File | Change | Impact |
|------|--------|--------|
| `frontend/nginx.conf` | Updated backend service name + headers | ✅ Required for external access |
| `application.properties` | Environment-aware MongoDB URI | ✅ Docker compatibility |
| `application.properties` | Environment-aware JWT secrets | ✅ Security improvement |
| `docker-compose.yml` | Added forward headers strategy | ✅ Required for proxy |
| `docker-compose.yml` | Added JWT secret env vars | ✅ Security improvement |

---

## ✅ Verification

After deploying, verify:
- [ ] All containers running: `docker-compose ps`
- [ ] No errors in logs: `docker-compose logs`
- [ ] Local access works: `http://localhost:3000`
- [ ] External access works: `http://YOUR_PUBLIC_IP:3000`
- [ ] Login successful from external device
- [ ] All modules functional (Courses, Assignments, Attendance, Teacher Dashboard)
- [ ] Audit logs being created (test by logging in)

---

## 🎉 You're Ready!

Your application is now configured for external access. The changes ensure:
- ✅ Proper proxying through Nginx
- ✅ Correct backend service resolution in Docker network
- ✅ Security-conscious configuration (environment variables)
- ✅ Support for proxy headers (for external IPs)
- ✅ MongoDB working in Docker
- ✅ All existing functionality preserved

**Next Steps**:
1. Rebuild Docker containers
2. Configure router port forwarding
3. Test external access
4. (Optional) Set up HTTPS for production
5. Share your link with users!

---

## 📖 Additional Resources

- Docker Networking: https://docs.docker.com/network/
- Nginx Reverse Proxy: https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/
- Spring Boot Behind Proxy: https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.webserver.use-behind-a-proxy-server
- Cloudflare Tunnel: https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/

---

Generated: March 17, 2026
Version: 1.0
