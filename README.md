# ticketr.io
![ticketr-io-workflow](https://github.com/aquiliz/ticketr-io/actions/workflows/ci.yml/badge.svg)

A cloud-native, microservice-based application for booking plane tickets.
It demonstrates how several microservices can be wired together using both async and sync means to form
a cohesive application. It is not a business-ready product, since some aspects depend on the 
specific business-case implementation and the airline billing policies.
All microservices are placed in the same git repo just for GitHub tidiness. In a real business scenario, ideally each one will be in
a separate git repository. 

The following microservices are in place:
- api-gateway - REST API entry point. It routes requests to the responsible microservice and utilizes circuit breaker functionality
- service-registry - Registers and coordinates the sync communication.
- ticket-booking-service - The entry point. Provides REST API for booking new tickets. Stores them in MongoDb
- ticket-pricing-service - Calculates the price of the plane ticket depending on numerous factors. Invoked synchronously via REST.
- invoicing-service - Listens for events for newly booked tickets on the Kafka topic, generates pdf invoice documents and sends them by email to the recipient.

## How to run the project locally

### Option 1: using Docker compose
#### Prerequisites: Docker
- From repository's root execute:
 
  ``docker-compose up -d``

### Option 2: using Kubernetes
#### Prerequisites: Kubernetes
- From repository's root execute:

  ``kubectl apply -f k8s-scripts/main``
- Expose the port of the API gateway

  ``kubectl port-forward service/api-gateway 8080:8080``
- (**Optional**) to enable consolidated logging based on Elastic Stack, execute:

  ``kubectl apply -f k8s-scripts/elasticStack``

  ``kubectl port-forward service/kibana 5601:5601``

  You can now access Kibana at: http://localhost:5601/ Create a new index pattern: ``filebeat*`` and explore the logs from all microservices utilizing ElasticSearch's powerful search engine


## Optional configurations
The following environment variables can be set for each service to override the default settings. Default values are 
meant to be used only for testing on a local dev environment.

#### 

#### api-gateway
- EUREKA_URI - full URL of the Eureka service registry. default: http://localhost:8761/eureka

#### ticket-booking-service
- MONGO_URI - a [connection string URI](https://www.mongodb.com/docs/manual/reference/connection-string/) for MongoDB. default: mongodb://root:rootpassword@localhost:27017/ticket_bookings?authSource=admin
- EUREKA_URI - full URL of the Eureka service registry. default: http://localhost:8761/eureka
- KAFKA_URI - host and port of the running Kafka instance. default: localhost:29090

#### ticket-pricing-service
- EUREKA_URI - full URL of the Eureka service registry. default: http://localhost:8761/eureka

#### invoicing-service

- ENABLE_EMAIL_SENDING - if true, each issued invoice will be sent by email. Default: false
- SMTP_HOST - the host of the SMTP server. Default: smtp.mailtrap.io  (good for testing)
- SMTP_PORT - the port of the SMTP server. Default: 25  (mailtrap's default port)
- SMTP_USERNAME - smtp server's username. Default: test
- SMTP_PASSWORD - smtp server's password. Default: test
- KAFKA_URI - host and port of the running Kafka instance. default: localhost:29090

## Useful tips
- To explore the booking API, open the Swagger UI page of the booking service: http://localhost:8083/swagger-ui/index.html
    - In addition, the OpenAPI spec file can be viewed at: http://localhost:8083/v3/api-docs/
- After starting the service registry, Eureka's dashboard can be accessed at: http://localhost:8761/ . Each registered
  microservice will be visible there.
- For debugging purpose, a console consumer can be opened on the Kafka topic (ticket-booking-topic):
    - docker exec -it ``<kafka-container-id>`` bash
    - cd ../../bin
    - ./kafka-console-consumer --bootstrap-server localhost:9092 --topic ticket-booking-topic --from-beginning
- Example CURL to make a new plane ticket booking:
    ```
      curl --location --request POST 'http://localhost:8080/booking' \
        --header 'Content-Type: application/json' \
        --data-raw '{
        "userId": "user123",
        "originAirport": "SYD",
        "destinationAirport": "IST",
        "passengers": [
          {
            "passportNumber": "12345678",
            "firstName": "John",
            "lastName": "Doe",
            "billingPerson": true,
            "piecesOfCheckedBaggage": 1,
            "bookedSeats": [
              {
                "seatNumber": "25A",
                "seatClass": "ECONOMY",
                "flightNumber": "ML556"
              }
             ]
          }
        ],
        "flights": [
          {
           "flightNumber": "ML556",
           "scheduledTime": "2023-03-01 12:12:00",
           "flightOrigin": "SYD",
           "flightDestination": "KUL"
          },
          {
           "flightNumber": "TK099",
           "scheduledTime": "2023-03-02 10:00:00",
           "flightOrigin": "KUL",
           "flightDestination": "IST"
          }
        ]
        }
    ```
