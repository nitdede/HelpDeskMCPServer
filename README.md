# HelpDeskMCPServer

[![GitHub Repository](https://img.shields.io/badge/GitHub-HelpDeskMCPServer-green?logo=github)](https://github.com/nitdede/HelpDeskMCPServer)
[![Main Application](https://img.shields.io/badge/GitHub-AIPoweredClaimUnderwriter-blue?logo=github)](https://github.com/nitdede/AIPoweredClaimUnderwriter)

A specialized **Model Context Protocol (MCP) Server** that provides AI agents with powerful tools for help desk operations, claim lookup, ticket management, and policy document retrieval. This server works as a companion service to the [AI-Powered Claim Underwriter](https://github.com/nitdede/AIPoweredClaimUnderwriter) application.

## 🏗️ Architecture Overview

The HelpDeskMCPServer implements the Model Context Protocol to expose specialized tools that AI agents can invoke:

```
┌─────────────────────────────┐     ┌──────────────────────────────┐
│   AIPoweredClaimUnderwriter │────▶│      HelpDeskMCPServer       │
│                             │     │                              │
│  • Spring AI MCP Client     │     │  • MCP Tools Implementation  │
│  • ChatClient with Tools    │     │  • Database Access           │
│  • Streaming Chat API       │     │  • Vector Search             │
└─────────────────────────────┘     └──────────────────────────────┘
                │                                    │
                ▼                                    ▼
        ┌──────────────────┐              ┌──────────────────┐
        │   PostgreSQL     │              │   Vector Store   │
        │                  │              │                  │
        │ • Claim Data     │              │ • Policy Chunks  │
        │ • Decisions      │              │ • Embeddings     │
        │ • Evidence       │              │ • Similarity     │
        │ • Help Tickets   │              │                  │
        └──────────────────┘              └──────────────────┘
```

## ✨ Key Features

### 🛠️ MCP Tools
- **HelpUser Tool**: Retrieve claim decision details and evidence
- **CreateTicket Tool**: Generate help desk tickets for customer issues
- **GetAllTickets Tool**: Access customer's ticket history
- **ReadUserPolicy Tool**: RAG-powered policy document search

### 🔍 Vector Search Integration
- **Semantic Search**: Uses pgVector for policy document similarity search
- **RAG Capabilities**: Retrieves relevant policy chunks based on customer queries
- **Metadata Filtering**: Ensures policy results match specific customers
- **Performance Optimized**: Async execution with caching and timeout protection

### 💾 Data Management
- **Claim Decision Access**: Direct database queries for claim information
- **Ticket Management**: CRUD operations for help desk tickets
- **Evidence Tracking**: Maintains audit trail of claim evidence
- **Customer Context**: Links tickets and decisions to specific customers

### ⚡ Performance Features
- **Async Processing**: Non-blocking vector search operations
- **Smart Caching**: RAG cache for improved response times
- **Timeout Protection**: Prevents hanging on slow vector operations
- **Thread Pool Management**: Dedicated executors for different operations

## 🛠️ Technology Stack

### Backend Framework
- **Spring Boot 3.5.10** - Main application framework
- **Spring AI 1.1.2** - AI integration and MCP server capabilities
- **Spring Data JPA** - Database access layer
- **Spring AI MCP Server** - Model Context Protocol implementation

### Communication Modes
- **STDIO Mode (Local)**: Uses standard input/output for process-to-process communication
- **HTTP Mode (Docker)**: Uses HTTP endpoints for network-based communication
- Profile-based configuration automatically selects the appropriate mode

### Database & Search
- **PostgreSQL** - Primary database with pgVector extension
- **pgVector** - Vector similarity search for policy documents
- **JPA Repositories** - Type-safe database queries

### AI & ML Integration
- **Model Context Protocol** - Tool integration standard for AI agents
- **Vector Embeddings** - OpenAI text-embedding-3-small compatible
- **Semantic Search** - Policy document retrieval using similarity search

### Development Tools
- **Lombok** - Code generation and boilerplate reduction
- **Maven** - Build automation and dependency management
- **Docker** - Containerization support

## 📋 Prerequisites

- **Java 21+** (configured in pom.xml)
- **Maven 3.6+** (wrapper included: `./mvnw`)
- **PostgreSQL 15+** with **pgVector** extension
- **AIPoweredClaimUnderwriter** ([Main Application](https://github.com/nitdede/AIPoweredClaimUnderwriter)) - The primary application that uses this MCP server

## 🚀 Quick Start

### Running Modes

The server operates differently based on the deployment environment:

#### Local Development (STDIO Mode)
```bash
# 1. Clone repository
git clone https://github.com/nitdede/HelpDeskMCPServer.git
cd HelpDeskMCPServer

# 2. Build the MCP server
./mvnw clean package

# 3. Run with default 'local' profile (STDIO mode)
./mvnw spring-boot:run

# 4. Logs are in /tmp/helpdesk-mcp-server.log (NOT console)
tail -f /tmp/helpdesk-mcp-server.log

# 5. In another terminal, start the main application
cd ../AIPoweredClaimUnderwriter
export MCP_CLIENT_ENABLED=true
./mvnw spring-boot:run
```

**Important**: You won't see logs in the console when running locally. This is intentional - STDOUT is reserved for MCP JSON messages.

#### Docker Deployment (HTTP Mode)
```bash
# Start all services via docker-compose from main application
cd AIPoweredClaimUnderwriter
docker-compose up -d

# View MCP server logs (console logging enabled in docker mode)
docker-compose logs -f helpdesk-mcp

# Check health
curl http://localhost:8082/mcp
```

## 🔧 Configuration

### Communication Modes

The MCP server supports two communication modes, automatically selected based on the active Spring profile:

#### STDIO Mode (Local Development - Default)
**When**: Running locally without Docker
**How**: Standard input/output (process-to-process communication)
**Profile**: `local` or no profile (default)

**Critical Requirement**: STDOUT must remain clean for MCP JSON protocol messages.

**Logging Strategy**:
```xml
<!-- logback-spring.xml for local profile -->
<springProfile name="!docker">
  <root level="INFO">
    <appender-ref ref="FILE" />  <!-- File only, NO console -->
  </root>
</springProfile>
```

- All logs written to `/tmp/helpdesk-mcp-server.log`
- **No console output** (would corrupt MCP JSON messages)
- Check logs with: `tail -f /tmp/helpdesk-mcp-server.log`

#### HTTP Mode (Docker Deployment)
**When**: Running in Docker containers
**How**: HTTP REST endpoints for network communication
**Profile**: `docker`

**Logging Strategy**:
```xml
<!-- logback-spring.xml for docker profile -->
<springProfile name="docker">
  <root level="INFO">
    <appender-ref ref="CONSOLE" />  <!-- Console enabled -->
    <appender-ref ref="FILE" />
  </root>
</springProfile>
```

- Logs to both console and file
- Console logs visible via `docker-compose logs -f helpdesk-mcp`
- STDOUT not used for protocol, safe to log to console

### Why Different Logging?

| Aspect | STDIO Mode (Local) | HTTP Mode (Docker) |
|--------|-------------------|--------------------|
| Protocol Channel | STDOUT/STDIN | HTTP Network |
| Console Logging | **Disabled** 🚫 | **Enabled** ✅ |
| Reason | STDOUT used for JSON messages | STDOUT free for logs |
| Log Location | `/tmp/helpdesk-mcp-server.log` | Console + File |
| Visibility | `tail -f` log file | `docker logs` |

### Database Configuration
The MCP server connects to the same PostgreSQL database as the main application. Configure connection details in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/insurance_ai
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: validate  # Don't create schema, use existing
```

### Vector Store Configuration
```yaml
spring:
  ai:
    vectorstore:
      pgvector:
        table-name: policy_chunks
        dimensions: 1536  # OpenAI embedding dimensions
        initialize-schema: false  # Schema managed by main app
```

### Thread Pool Configuration
```java
@Configuration
public class ExecutorConfig {
    @Bean(name = "vectorTaskExecutor")
    public Executor vectorTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("vector-search-");
        executor.initialize();
        return executor;
    }
}
```

## 🛠️ Available MCP Tools

### HelpUser Tool
Retrieves complete claim decision information including amounts, reasons, and evidence.

```java
@Tool(name = "helpUser", description = "Provide assistance to the user regarding their claim issue by reading the claim decision details")
public ClaimDecision helpUser(String claimId)
```

**Usage**: When AI needs to answer questions about specific claims, it automatically invokes this tool to fetch accurate claim data.

### CreateTicket Tool
Creates new help desk tickets for customer issues that require human intervention.

```java
@Tool(name = "createTicket", description = "Create a help desk ticket for the user's issue or problem")
public String createTicket(String userName, String issueDescription)
```

**Usage**: AI escalates complex issues to human support by creating tracked tickets.

### GetAllTickets Tool
Retrieves customer's complete ticket history for context and follow-up.

```java
@Tool(name = "getAllTickets", description = "Get all help desk tickets for the user")
public List<HelpDeskTicket> getAllTickets(String userName)
```

**Usage**: Provides conversation context by showing previous support interactions.

### ReadUserPolicy Tool (RAG Integration)
Performs semantic search on policy documents to retrieve relevant coverage information.

```java
@Tool(name = "readUserPolicy", description = "Read the user's policy document based on policy number and customer ID")
public String readUserPolicy(String policyNumber, String patientName)
```

**How it works**:
1. **Vector Search**: Queries the `policy_chunks` table using similarity search
2. **Metadata Filtering**: Filters results by `policyNumber` and `customerId` 
3. **Content Assembly**: Returns top 3 most relevant policy text chunks
4. **Caching**: Stores results for improved performance on subsequent queries

## 💾 Database Schema

The MCP server works with these key database tables:

### claim_decisions
Stores claim adjudication results:
```sql
CREATE TABLE claim_decisions (
  id BIGSERIAL PRIMARY KEY,
  claim_id BIGINT NOT NULL,
  decision VARCHAR(30) NOT NULL,
  payable_amount DECIMAL(10,2),
  reasons JSONB,
  letter TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### helpdesk_tickets
Manages customer support tickets:
```sql
CREATE TABLE helpdesk_tickets (
  id BIGSERIAL PRIMARY KEY,
  customer_name VARCHAR(255) NOT NULL,
  issue_description TEXT NOT NULL,
  status VARCHAR(50) NOT NULL,
  priority VARCHAR(50) NOT NULL,
  assigned_to VARCHAR(255),
  created TIMESTAMP NOT NULL,
  eta TIMESTAMP
);
```

### policy_chunks (Vector Store)
Stores policy document embeddings:
```sql
CREATE TABLE policy_chunks (
  id UUID PRIMARY KEY,
  content TEXT,
  metadata JSONB,
  embedding vector(1536)  -- OpenAI embedding dimensions
);
```

## 🎯 Usage Examples

### Integration with Main Application
The main application automatically connects to this MCP server and makes tools available to AI agents:

```java
// In AIPoweredClaimUnderwriter
@Bean(name = "helpDeskClient")
public ChatClient helpDeskClient(ChatClient.Builder clientBuilder, 
                                ToolCallbackProvider toolCallbackProvider) {
    return clientBuilder
            .defaultToolCallbacks(toolCallbackProvider)  // MCP tools here
            .defaultSystem(systemPromptTemplate)
            .build();
}
```

### Example AI Conversation Flow
```
User: "Can you check the status of my claim 12345?"

AI Process:
1. Recognizes need for claim information
2. Invokes helpUser("12345") tool via MCP
3. Receives ClaimDecision object with full details
4. Responds with specific claim status and amounts

User: "Does my policy cover dental work?"

AI Process:
1. Recognizes policy coverage question
2. Invokes readUserPolicy("POL-001", "John Doe") tool via MCP
3. Receives relevant policy text about dental coverage
4. Provides accurate coverage information
```

## 🐳 Docker Deployment

### Using Docker Compose (Recommended)

The MCP server is designed to run as part of the main application's docker-compose setup:

```bash
# From the main application directory
cd AIPoweredClaimUnderwriter

# Start all services (includes helpdesk-mcp, postgres, app)
docker-compose up -d

# Check health status
docker-compose ps

# View MCP server logs (console logging enabled in docker mode)
docker-compose logs -f helpdesk-mcp

# Stop all services
docker-compose down
```

### Health Check Configuration

The docker-compose includes a health check for the MCP server:

```yaml
helpdesk-mcp:
  healthcheck:
    test: ["CMD-SHELL", "curl -s -o /dev/null -w '%{http_code}' http://localhost:8082/mcp | grep -E '^[0-9]{3}$' || exit 1"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 30s
```

This ensures:
- The main application waits for MCP server to be healthy before starting
- Automatic restart if health check fails
- 30-second grace period for startup

### Environment Variables

The following environment variables are set via docker-compose:

```yaml
environment:
  SPRING_PROFILES_ACTIVE: "docker"  # Enables HTTP mode + console logging
  POSTGRES_HOST: "postgres"
  POSTGRES_PORT: "5432"
  POSTGRES_DB: "${POSTGRES_DB}"
  POSTGRES_USER: "${POSTGRES_USER}"
  POSTGRES_PASSWORD: "${POSTGRES_PASSWORD}"
  OPENAI_API_KEY: "${OPENAI_API_KEY}"
```

### Build Docker Image
```bash
# Build the Docker image
docker build -t helpdesk-mcp-server .
```

### Run with Docker
```bash
# Run the MCP server container
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/insurance_ai \
  -e SPRING_DATASOURCE_USERNAME=username \
  -e SPRING_DATASOURCE_PASSWORD=password \
  helpdesk-mcp-server
```

### Docker Compose Integration
```yaml
version: '3.8'
services:
  helpdesk-mcp-server:
    build: .
    ports:
      - "8082:8082"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/insurance_ai
    depends_on:
      - postgres
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8082/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

## 🧪 Testing

### Run Unit Tests
```bash
./mvnw test
```

### Integration Testing
```bash
# Run with test profile
./mvnw test -Dspring.profiles.active=test
```

### Manual Testing
```bash
# Test MCP server health (HTTP mode only - Docker)
curl http://localhost:8082/actuator/health
curl http://localhost:8082/mcp

# For STDIO mode (local), test via the main application's help desk feature
# Tools are invoked automatically by AI agents through STDIO communication

# Check logs
# Local mode:
tail -f /tmp/helpdesk-mcp-server.log

# Docker mode:
docker-compose logs -f helpdesk-mcp
```

## 📊 Monitoring & Observability

### Health Checks
- **Application Health**: `/actuator/health`
- **Database Connectivity**: Included in health endpoint
- **Vector Store Status**: Monitored through search operations

### Logging
- **Tool Invocation**: Detailed logging of MCP tool calls
- **Vector Search**: Performance metrics for similarity searches
- **Database Operations**: Query execution times and results

### Performance Metrics
- **Vector Search Latency**: Timeout-protected async operations
- **Cache Hit Rates**: RAG cache effectiveness
- **Tool Execution Times**: MCP tool performance tracking

## 🔒 Security Considerations

- **Database Access**: Read-only access to claim decisions, full access to tickets
- **Input Validation**: All tool parameters are validated
- **Timeout Protection**: Vector searches have configurable timeouts
- **Error Handling**: Sensitive information not exposed in error responses

## 🤝 Integration with Main Application

### Prerequisites
1. Main application must be running: [AIPoweredClaimUnderwriter](https://github.com/nitdede/AIPoweredClaimUnderwriter)
2. Shared PostgreSQL database must be accessible
3. MCP client must be enabled in main application: `MCP_CLIENT_ENABLED=true`

### Connection Flow
1. **MCP Server Startup**: This server registers available tools
2. **Client Connection**: Main application connects via Spring AI MCP Client
3. **Tool Registration**: Tools become available to ChatClient
4. **AI Invocation**: AI agents automatically call tools when needed

### Development Workflow
```bash
# Terminal 1: Start PostgreSQL (if not already running)
cd AIPoweredClaimUnderwriter
docker-compose up postgres -d

# Terminal 2: Start the MCP server (STDIO mode, logs to file)
cd HelpDeskMCPServer
./mvnw spring-boot:run
# Note: No console output expected - check /tmp/helpdesk-mcp-server.log

# Terminal 3: Monitor MCP server logs
tail -f /tmp/helpdesk-mcp-server.log

# Terminal 4: Start the main application
cd AIPoweredClaimUnderwriter
export MCP_CLIENT_ENABLED=true
./mvnw spring-boot:run

# Terminal 5: Test the integration
curl -X POST http://localhost:8081/api/helpdesk-call/helpUser \
  -H "Content-Type: application/json" \
  -d '{"claimId":"123","issueDescription":"Question about my claim","customerName":"John Doe","policyNumber":"POL-001"}'
```

## � Troubleshooting

### No Logs Appearing in Console (Local Mode)
**Problem**: Running `./mvnw spring-boot:run` shows no log output after startup.

**This is normal!** In local mode (STDIO), the MCP server **intentionally** does not log to console.

**Why?**
- STDOUT is used for MCP JSON protocol messages
- Console logs would corrupt the message stream
- The main application would fail to parse JSON responses

**Solution:**
```bash
# Check logs in the file instead
tail -f /tmp/helpdesk-mcp-server.log

# Or watch logs continuously
watch -n 1 tail -20 /tmp/helpdesk-mcp-server.log
```

### MCP Tools Not Found
**Problem**: Main application reports "MCP tools not available" or AI doesn't have access to tools.

**Solutions:**
1. **Ensure MCP Server is Running**:
   ```bash
   # Check if process is running
   ps aux | grep HelpDeskMCPServer
   
   # Check log file for errors
   tail -f /tmp/helpdesk-mcp-server.log
   ```

2. **Enable MCP Client in Main App**:
   ```bash
   export MCP_CLIENT_ENABLED=true
   ```

3. **Verify Profile Configuration**:
   - Local: Should use default profile (STDIO mode)
   - Docker: Must use `docker` profile (HTTP mode)

### Database Connection Errors
**Problem**: MCP server fails to connect to PostgreSQL.

**Solutions:**
1. **Ensure PostgreSQL is Running**:
   ```bash
   # Check if postgres is running
   docker-compose ps postgres
   
   # Start postgres if needed
   cd AIPoweredClaimUnderwriter
   docker-compose up postgres -d
   ```

2. **Verify Database Credentials**:
   - Check `application.yml` or `application-local.yml`
   - Ensure credentials match your PostgreSQL setup

3. **Test Connection**:
   ```bash
   psql -h localhost -U your_username -d insurance_ai
   ```

### Health Check Failing in Docker
**Problem**: `docker-compose ps` shows helpdesk-mcp as unhealthy.

**Solutions:**
1. **Check MCP Endpoint**:
   ```bash
   # Test the health check endpoint
   curl http://localhost:8082/mcp
   curl http://localhost:8082/actuator/health
   ```

2. **View Container Logs**:
   ```bash
   docker-compose logs helpdesk-mcp
   ```

3. **Increase Start Period**:
   - Slow systems may need more time to start
   - Edit `docker-compose.yml` and increase `start_period: 30s` to `60s`

### Vector Search Not Working
**Problem**: `readUserPolicy` tool returns no results.

**Solutions:**
1. **Verify pgVector Extension**:
   ```sql
   -- Connect to database
   psql insurance_ai
   
   -- Check extension
   \dx
   
   -- Install if missing
   CREATE EXTENSION IF NOT EXISTS vector;
   ```

2. **Check Policy Chunks Table**:
   ```sql
   SELECT COUNT(*) FROM policy_chunks;
   SELECT * FROM policy_chunks LIMIT 5;
   ```

3. **Re-ingest Policy Documents**:
   - Use main application's "Ingest Policy" feature
   - Ensure embeddings are generated correctly

## �🚧 Future Enhancements

- [ ] **Additional Tools**: Weather data, external API integrations
- [ ] **Advanced RAG**: Multi-modal document search (images, tables)
- [ ] **Performance Optimization**: Redis caching for frequent queries
- [ ] **Security Enhancement**: OAuth2 authentication for tool access
- [ ] **Monitoring Dashboard**: Real-time tool usage metrics
- [ ] **Multi-tenant Support**: Isolated data access per organization

## 🤝 Contributing

1. Fork the [HelpDeskMCPServer](https://github.com/nitdede/HelpDeskMCPServer) repository
2. Create a feature branch: `git checkout -b feature/new-tool`
3. Implement new MCP tools in the `tools` package
4. Add comprehensive tests for tool functionality
5. Update documentation and examples
6. Submit a Pull Request

### Adding New Tools
```java
@Tool(name = "newTool", description = "Description of what the tool does")
public ReturnType newTool(@JsonProperty(required = true) 
                         @JsonPropertyDescription("Parameter description") 
                         String parameter) {
    // Tool implementation
    return result;
}
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For questions or support:
- **Issues**: [Create a GitHub issue](https://github.com/nitdede/HelpDeskMCPServer/issues)
- **Main Application**: [AIPoweredClaimUnderwriter Issues](https://github.com/nitdede/AIPoweredClaimUnderwriter/issues)
- **Documentation**: Check the source code and JavaDoc comments
- **Integration Help**: Refer to the main application's README for end-to-end setup

## 🔗 Related Projects

- **Main Application**: [AIPoweredClaimUnderwriter](https://github.com/nitdede/AIPoweredClaimUnderwriter) - The primary AI-powered claim processing system
- **Spring AI**: [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/) - Framework documentation
- **Model Context Protocol**: [MCP Specification](https://modelcontextprotocol.io/) - Protocol specification

---

**Built with ❤️ using Spring Boot, Spring AI, and Model Context Protocol**

*This MCP server is designed to work seamlessly with the [AI-Powered Claim Underwriter](https://github.com/nitdede/AIPoweredClaimUnderwriter) application to provide intelligent help desk capabilities.*