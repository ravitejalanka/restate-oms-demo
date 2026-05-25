# Overview
Implement a basic health check endpoint at `/health` in the API Gateway module that returns a JSON response `{"status": "ok"}` with HTTP status code 200 OK. The endpoint will be publicly accessible and does not require any additional health checks beyond the basic status.

# Files to Modify / Create
- `/workspace/repo/order-api-gateway/src/main/kotlin/com/learning/restate/with/boot/gateway/controller/OrderRestController.kt`
  - Add a new GET endpoint `/health` that returns the required JSON response.

# Implementation Steps
1. Open the file `/workspace/repo/order-api-gateway/src/main/kotlin/com/learning/restate/with/boot/gateway/controller/OrderRestController.kt`.
2. Add a new method annotated with `@GetMapping("/health")` that:
   - Returns a `Map<String, String>` containing `{"status": "ok"}`.
   - Uses `@ResponseBody` to ensure proper JSON serialization.
3. Ensure the method has public access (no security annotations).
4. Save the file.
5. Build the project to verify compilation.

# Test Cases
1. Start the API Gateway application.
2. Send a GET request to `http://<host>:<port>/health`.
3. Verify the response:
   - Status code is 200 OK.
   - Response body is `{"status": "ok"}`.
4. Confirm the endpoint is accessible without authentication.