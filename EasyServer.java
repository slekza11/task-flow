java 
import com.sun.net.httpserver.HttpServer; 
import com.sun.net.httpserver.HttpHandler; 
import com.sun.net.httpserver.HttpExchange; 
import java.net.InetSocketAddress; 
import java.io.OutputStream; 
import java.io.BufferedReader; 
import java.io.InputStreamReader; 
import java.io.FileInputStream; 
import java.io.IOException; 
import java.nio.charset.StandardCharsets; 
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.List; 
import java.util.Map; 
public class EasyServer { 
    private static List<Map<String, Object>> tasks = new ArrayList<>(); 
    private static int nextId = 1; 
    private static final String FRONTEND_PATH = "../frontend/"; 
    public static void main(String[] args) throws Exception { 

        // GET YOUR IP ADDRESS - Run 'ipconfig' in terminal 

        String localIP = "192.168.0.224"; // CHANGE THIS TO YOUR IP! 

         

        // Sample tasks 

        addTask("Complete NQF Level 4 Project", "Build full-stack app", "2025-12-15", "High"); 

        addTask("Learn Java Backend", "Understand REST APIs", "2025-12-10", "Medium"); 

        addTask("Test on Phone", "Access from mobile device", "2025-12-05", "Low"); 

         

        HttpServer server = HttpServer.create(new InetSocketAddress(4567), 0); 

        server.createContext("/api/tasks", new TaskAPIHandler()); 

        server.createContext("/", new FrontendHandler()); 

        server.setExecutor(null); 

        server.start(); 

         

        System.out.println("\n╔════════════════════════════════════════════════════╗"); 

        System.out.println("║     🚀 TASKFLOW JAVA SERVER RUNNING                ║"); 

        System.out.println("╠════════════════════════════════════════════════════╣"); 

        System.out.println("║  Computer: http://localhost:4567                   ║"); 

        System.out.println("║  Phone:    http://" + localIP + ":4567              ║"); 

        System.out.println("║  Press Ctrl+C to stop                              ║"); 

        System.out.println("╚════════════════════════════════════════════════════╝\n"); 

    } 

     

    private static void addTask(String title, String desc, String date, String priority) { 

        Map<String, Object> task = new HashMap<>(); 

        task.put("id", nextId++); 

        task.put("title", title); 

        task.put("description", desc); 

        task.put("due_date", date); 

        task.put("priority", priority); 

        task.put("completed", 0); 

        tasks.add(task); 

    } 

     

    static class FrontendHandler implements HttpHandler { 

        public void handle(HttpExchange exchange) throws IOException { 

            String path = exchange.getRequestURI().getPath(); 

            if (path.equals("/")) path = "/index.html"; 

             

            String filePath = FRONTEND_PATH + path; 

            java.io.File file = new java.io.File(filePath); 

             

            if (file.exists()) { 

                String contentType = path.endsWith(".html") ? "text/html" :  

                                    path.endsWith(".css") ? "text/css" :  

                                    path.endsWith(".js") ? "application/javascript" : "text/plain"; 

                exchange.getResponseHeaders().set("Content-Type", contentType); 

                exchange.sendResponseHeaders(200, file.length()); 

                 

                FileInputStream fis = new FileInputStream(file); 

                OutputStream os = exchange.getResponseBody(); 

                byte[] buffer = new byte[4096]; 

                int bytesRead; 

                while ((bytesRead = fis.read(buffer)) != -1) os.write(buffer, 0, bytesRead); 

                fis.close(); 

                os.close(); 

            } else { 

                String response = "404 - Not Found"; 

                exchange.sendResponseHeaders(404, response.length()); 

                exchange.getResponseBody().write(response.getBytes()); 

                exchange.getResponseBody().close(); 

            } 

        } 

    } 

     

    static class TaskAPIHandler implements HttpHandler { 

        public void handle(HttpExchange exchange) throws IOException { 

            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); 

            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); 

             

            if (exchange.getRequestMethod().equals("OPTIONS")) { 

                exchange.sendResponseHeaders(204, -1); 

                return; 

            } 

             

            String method = exchange.getRequestMethod(); 

            try { 

                if (method.equals("GET")) handleGet(exchange); 

                else if (method.equals("POST")) handlePost(exchange); 

                else if (method.equals("PUT")) handlePut(exchange); 

                else if (method.equals("DELETE")) handleDelete(exchange); 

            } catch (Exception e) { 

                sendResponse(exchange, "Error", 500); 

            } 

        } 

         

        private void handleGet(HttpExchange exchange) throws IOException { 

            String json = "["; 

            for (int i = 0; i < tasks.size(); i++) { 

                if (i > 0) json += ","; 

                Map<String, Object> t = tasks.get(i); 

                json += "{\"id\":" + t.get("id") + ",\"title\":\"" + t.get("title") + "\","; 

                json += "\"description\":\"" + (t.get("description") == null ? "" : t.get("description")) + "\","; 

                json += "\"due_date\":\"" + (t.get("due_date") == null ? "" : t.get("due_date")) + "\","; 

                json += "\"priority\":\"" + t.get("priority") + "\","; 

                json += "\"completed\":" + t.get("completed") + "}"; 

            } 

            json += "]"; 

            sendResponse(exchange, json, 200); 

        } 

         

        private void handlePost(HttpExchange exchange) throws IOException { 

            String body = readBody(exchange); 

            Map<String, Object> newTask = new HashMap<>(); 

            newTask.put("id", nextId++); 

            newTask.put("title", extractValue(body, "title")); 

            newTask.put("description", extractValue(body, "description")); 

            newTask.put("due_date", extractValue(body, "due_date")); 

            newTask.put("priority", extractValue(body, "priority")); 

            newTask.put("completed", 0); 

            tasks.add(newTask); 

            sendResponse(exchange, "{\"status\":\"ok\"}", 201); 

        } 

         

        private void handlePut(HttpExchange exchange) throws IOException { 

            String path = exchange.getRequestURI().getPath(); 

            int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1)); 

            String body = readBody(exchange); 

            int completed = body.contains("\"completed\":1") ? 1 : 0; 

             

            for (Map<String, Object> task : tasks) { 

                if ((int)task.get("id") == id) { 

                    task.put("completed", completed); 

                    break; 

                } 

            } 

            sendResponse(exchange, "{\"status\":\"ok\"}", 200); 

        } 

         

        private void handleDelete(HttpExchange exchange) throws IOException { 

            String path = exchange.getRequestURI().getPath(); 

            int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1)); 

            tasks.removeIf(task -> (int)task.get("id") == id); 

            sendResponse(exchange, "{\"status\":\"ok\"}", 200); 

        } 

         

        private String readBody(HttpExchange exchange) throws IOException { 

            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)); 

            StringBuilder body = new StringBuilder(); 

            String line; 

            while ((line = br.readLine()) != null) body.append(line); 

            return body.toString(); 

        } 

         

        private String extractValue(String json, String key) { 

            String search = "\"" + key + "\":\""; 

            int start = json.indexOf(search); 

            if (start == -1) return ""; 

            start += search.length(); 

            int end = json.indexOf("\"", start); 

            return end == -1 ? "" : json.substring(start, end); 

        } 

         

        private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException { 

            exchange.getResponseHeaders().set("Content-Type", "application/json"); 

            exchange.sendResponseHeaders(statusCode, response.getBytes().length); 

            OutputStream os = exchange.getResponseBody(); 

            os.write(response.getBytes()); 

            os.close(); 

        } 

    } 

} 