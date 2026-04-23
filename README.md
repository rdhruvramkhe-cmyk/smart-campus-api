# smart-campus-api
# Smart Campus Sensor & Room Management API

This is a RESTful API I built using JAX-RS (Jersey) with Grizzly as the embedded server. It manages rooms, sensors and sensor readings for a university "Smart Campus" system. Everything runs in-memory using HashMaps and ArrayLists - no database.

## API Endpoints

Here's how the API is structured:

- `GET /api/v1` - Returns API info and links to other resources
- `GET /api/v1/rooms` - Lists all rooms
- `POST /api/v1/rooms` - Creates a new room
- `GET /api/v1/rooms/{roomId}` - Gets one room
- `DELETE /api/v1/rooms/{roomId}` - Deletes a room (only works if no sensors are in it)
- `GET /api/v1/sensors` - Lists all sensors (can filter with `?type=CO2`)
- `POST /api/v1/sensors` - Registers a new sensor (roomId must be valid)
- `GET /api/v1/sensors/{sensorId}/readings` - Gets reading history for a sensor
- `POST /api/v1/sensors/{sensorId}/readings` - Adds a new reading

There are 3 models: Room, Sensor, and SensorReading. The data is stored in a singleton DataStore class that holds everything in HashMaps.

For error handling I made custom exceptions with ExceptionMappers:
- 409 Conflict - when you try to delete a room that has sensors
- 422 Unprocessable Entity - when a sensor references a room that doesn't exist
- 403 Forbidden - when you try to post a reading to a sensor in MAINTENANCE mode
- 500 Internal Server Error - catch-all for any unexpected errors

There's also a LoggingFilter that logs every request and response automatically.

## How to Build and Run

```bash
1. Open the Project in NetBeans
2. Ensure that Java 11 or higher is installed
3. Run the main class.
```

Server starts at `http://localhost:8080/api/v1/`

## Sample curl Commands

1. Get API info:
```bash
curl -X GET http://localhost:8080/api/v1
```

2. Create a room:
```bash
curl -X POST http://localhost:8080/api/v1/rooms -H "Content-Type: application/json" -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

3. Get all rooms:
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

4. Get a specific room:
```bash
curl -X GET http://localhost:8080/api/v1/rooms/LIB-301
```

5. Create a sensor:
```bash
curl -X POST http://localhost:8080/api/v1/sensors -H "Content-Type: application/json" -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"LIB-301"}'
```

6. Filter sensors by type:
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

7. Post a reading:
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings -H "Content-Type: application/json" -d '{"value":22.5}'
```

8. Get readings for a sensor:
```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

9. Delete a room:
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

10. Try creating sensor with fake roomId (gives 422):
```bash
curl -X POST http://localhost:8080/api/v1/sensors -H "Content-Type: application/json" -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"FAKE-ROOM"}'
```

---

## Report

### Part 1: Service Architecture & Setup

**Q: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton?**

So by default JAX-RS creates a new instance of the resource class for every single request. It's not a singleton - each time someone hits an endpoint, a fresh object gets made, handles the request, and then gets thrown away.

This is a problem if you want to keep data around between requests. Like if I put a HashMap inside RoomResource to store rooms, it would be empty every time because the object gets recreated. That's why I made a separate DataStore class that follows the singleton pattern - it has a private constructor and a static `getInstance()` method that always gives back the same object. I also made `getInstance()` synchronized because the server handles multiple requests on different threads and I didn't want two threads accidentally creating two different DataStore objects at the same time.

So basically all my resource classes (RoomResource, SensorResource, SensorReadingResource) just call `DataStore.getInstance()` to access the shared data, and since it's the same object every time, the data sticks around.

**Q: Why is HATEOAS considered a hallmark of advanced RESTful design? How does it benefit client developers compared to static documentation?**

HATEOAS stands for Hypermedia as the Engine of Application State. The idea is that your API responses should include links telling the client where it can go next, instead of the client having to know all the URLs beforehand.

I implemented this in the discovery endpoint (`GET /api/v1`) - it returns JSON with a `resources` map that has links to `/api/v1/rooms` and `/api/v1/sensors`. So a client can start at the root and figure out the rest from there.

The main advantage over static docs is that if I change a URL, clients that follow links dynamically won't break - they just get the new URL. With static docs someone has to update them manually and then every developer has to change their code. It's kind of like how you browse the web - you click links instead of typing URLs from memory. It also makes the API self-documenting in a way, because new developers can just hit the root endpoint and see what's available.

### Part 2: Room Management

**Q: What are the implications of returning only IDs versus full room objects when listing rooms?**

If you return just IDs, the response is tiny but then the client has to make a separate GET request for every single room to get the details. With 500 rooms that's 500 extra requests - the N+1 problem. All those HTTP round trips would probably be worse than just sending the full data in one go.

I went with returning full room objects because they're pretty small anyway (just id, name, capacity and a list of sensor IDs). One request gives you everything. If the list got really huge you'd want to add pagination but for now this works fine.

**Q: Is the DELETE operation idempotent in your implementation?**

Yeah it is. Idempotent means calling it multiple times has the same effect as calling it once.

First time you DELETE `/api/v1/rooms/LIB-301`, the room gets removed and you get 204 No Content. Second time, the room's already gone so you get 404. The response code is different but that doesn't matter - idempotency is about the server state according to RFC 7231. And the state is the same either way: LIB-301 doesn't exist. Doesn't matter if you send the delete once or ten times, result is the same.

### Part 3: Sensor Operations & Linking

**Q: What happens if a client sends text/plain or application/xml when the method uses @Consumes(APPLICATION_JSON)?**

When I put `@Consumes(MediaType.APPLICATION_JSON)` on my POST method, JAX-RS checks the Content-Type header before even calling my code. If someone sends `text/plain` or `application/xml`, it doesn't match, so JAX-RS automatically returns 415 Unsupported Media Type. My method never runs.

This is useful because I don't have to write any code to check the format myself. And a 415 is way clearer than what would happen without the annotation - JAX-RS might try to parse XML as JSON and blow up with some confusing 500 error.

**Q: Why is @QueryParam better than putting the filter in the URL path?**

Path segments are for identifying resources. `/sensors/TEMP-001` makes sense because that's a specific sensor. But `/sensors/type/CO2` is weird because "type" and "CO2" aren't resources, you're just filtering.

Query params like `?type=CO2` are better because:
- They're optional. Leave it off and you get everything. With paths you'd need separate routes.
- You can combine them easily: `?type=CO2&status=ACTIVE`. Try doing that with paths and it gets messy fast: `/sensors/type/CO2/status/ACTIVE`. What if someone only wants to filter by status?
- It keeps the URL clean. `/api/v1/sensors` is clearly the sensors collection, the query string just changes which ones you see.

### Part 4: Deep Nesting with Sub-Resources

**Q: What are the benefits of the Sub-Resource Locator pattern?**

In my code, `SensorResource` has a method with `@Path("/{sensorId}/readings")` but no @GET or @POST on it. It just checks the sensor exists and returns a new `SensorReadingResource` object that handles the actual requests.

The biggest win is keeping things organized. Without this pattern all the sensor endpoints AND all the reading endpoints would be in one class. That gets messy quick, especially if you add more nested stuff later like alerts or config.

It also gives me a nice place to do validation. The locator method checks if the sensor exists before handing off to `SensorReadingResource`. So the readings class doesn't have to worry about that at all - it can just focus on doing its job.

Basically it's the single responsibility principle. `SensorResource` handles sensors, `SensorReadingResource` handles readings. Each class stays small and focused instead of having one giant controller with everything crammed in.

### Part 5: Error Handling, Exception Mapping & Logging

**Q: Why is 422 more accurate than 404 for a missing reference in a JSON payload?**

404 means the URL wasn't found. But when someone POSTs to `/api/v1/sensors` with a bad `roomId` in the body, the URL is fine - it's the data inside the JSON that's wrong.

422 Unprocessable Entity says "I got your request, the JSON is valid, but the data doesn't make sense." That's exactly what's happening - the server parsed the JSON fine but the roomId doesn't point to a real room. If I returned 404 the developer would waste time checking if the URL is correct instead of looking at the request body.

**Q: What are the security risks of exposing Java stack traces?**

Leaking stack traces is basically giving attackers a map of your application. They can see:

- What framework you're using (Jersey, version etc.) and look up known vulnerabilities for it
- Your package structure like `com.smartcampus.resource.SensorResource` which shows how the code is organized
- The execution flow - how requests get processed internally
- Sometimes file paths, database connection strings or table names if the error is database-related

Attackers can even trigger errors on purpose to collect different stack traces and gradually figure out the whole system.

That's why I have a GenericExceptionMapper that catches everything and just returns "An unexpected error occurred" to the client. The actual error details get logged on the server side where only I can see them.

**Q: Why use JAX-RS filters for logging instead of putting Logger.info() in every method?**

Because logging applies to every endpoint the same way. Writing `Logger.info()` in every single method in every resource class would mean a ton of duplicated code. And if I add a new endpoint I'd have to remember to add logging there too.

With my LoggingFilter class, it implements both ContainerRequestFilter and ContainerResponseFilter, so it automatically runs for every request and response. I wrote the logging code once and it covers everything. If I need to change the format later it's one file to edit instead of hunting through every method in the project.

It also keeps the resource classes clean - they just have business logic without logging calls scattered everywhere.
