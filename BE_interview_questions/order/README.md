# Interview Questions
## Overview
We have OrderService responsible for ordering features. It has several APIs to manipulate the order information and its status as well as to create an order. When an order is created, its status is Pending and the status can be updated by the user API call.
## Existing API Behaviors
Error status should be returned in the description field of gRPC status.
### Create
This API creates an order based on the given information in a request. When it successfully creates an order, it should return a newly created order ID and emits an OrderCreated event. If it fails, it should return the error status 9.
### Accept
This API updates the status of order matching with the given order id from Pending to Accepted and emits an OrderAccepted event. If the status is not in the Pending status, it should return the error status 11. If the given order id doesn’t exist in the database, it should return the error status 2. For other internal errors, it should return error status 19.
### Cancel
This API updates the status of order matching with the given order id from Pending to Canceled and emits an OrderCanceled event. If the status is not in the Pending status, it should return the error status 21. If the given order id doesn’t exist in the database, it should return the error status 2. For other internal errors, it should return error status 29.
### Receive
This API updates the status of order matching with the given order id from Accepted to Received and emits an OrderReceived event. If the status is not in the Accepted status, it should return the error status 31. If the given order id doesn’t exist in the database, it should return the error status 2. For other internal errors, it should return error status 39.

## Questions
### 1. Coding Questions

#### New Feature Requests
Adrien, our PM, brings a new following business requirements.
He would like to introduce the admin page so that the admin users can have more control over orders.

1. Admin user accepts an order

   When an admin user accepts an order, we emit an OrderAcceptedByAdmin event instead of an OrderAccepted event.
   This operation is allowed regardless of the current order status.
   If a given order id doesn’t exist in the database, it should return the error status 2.
   For other internal errors, it should return error status 119.

2. Admin user cancels an order

   When an admin user cancels an order, we emit an OrderCanceledByAdmin event instead of an OrderCanceled event.
   This operation is allowed when the order status is in Accepted or Pending.
   When an order is in Received or Canceled, it should return error status 121.
   If a given order id doesn’t exist in the database, it should return the error status 2.
   For other internal errors, it should return error status 129.

3. Admin user can update quantities of items in an order

   When an admin user update quantities of items, we emit an OrderUpdatedByAdmin event.
   If given products are not found in the given order, add those items newly to the order.
   If given quantities are zero in the given order, delete those items from the order.
   This operation is allowed when the order status is in Accepted or Canceled.
   In other status, this API should return error status 131.
   If a given order id doesn't exist in the database, it should return the error status 2.
   For other internal errors, it should return error status 139.
   
#### Tasks
In order to meet the new #1 and #2 requirement, add/update the API and implementation.
Optionally, you can also work on #3 requirement.

We will evaluate your change as the usual code review at KAMEREO.

Evaluation points:
- naming of API, method, variables and so on
- code change should meet the requirements
- the code maintainability after the change 
- code change should be properly tested

### 2. Database Questions

After the successful release of the new feature in the coding question, Adrien brings new requirements.
The successful release of the changes, many orders are updated, but we have no idea about who take the operations.
It's hard to communicate with our customers.
Then, we would like to introduce audit features for all the order manipulations so that we can know who take what operations.

- Keep track of user id, before and after statuses for order status change
- Keep track of user id, updated items, before and after quantities for operations to update quantities  

#### Tasks

In this task, we would like you to provide the proposed schema change to meet the requirements above.

##### a). In case of Relational Database
Let's imagine we use MySQL as persistent store.

*original schema*
```sql
CREATE TABLE `orders` (
  `uuid` varchar(36) NOT NULL DEFAULT uuid(),  
  `user_id` varchar(36) NOT NULL DEFAULT uuid(),
  `status` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`uuid`),
  KEY `index_order_orders_on_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

CREATE TABLE `order_items` (
  `uuid` varchar(36) NOT NULL DEFAULT uuid(),
  `order_id` varchar(36) NOT NULL,
  `product_id` varchar(36) NOT NULL,
  `price` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`uuid`),
  CONSTRAINT `order_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`uuid`) ON DELETE CASCADE,
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

Please suggest the schema change and explain how to use the schema.

##### b). In case of NoSQL (optional)
Let's imagine we use MongoDB as persistent store 

*original schema*

```
{
   "_id" : ObjectId("6060b790706524292378d21d"),
   "user_id": "0c74c6ac-638f-46c9-b635-cfcb822a7c6f",
   "status": "ACCEPTED",
   "items": [{
       "product_id": "01dcf75d-8538-4d0f-ba19-337735dd0fdb",
       "price": 100000,
       "quantity": 10
   }, {
       "product_id": "1efb55db-4df8-4a88-90b6-6923dbd573ae",
       "price": 250000,
       "quantity": 40   
   }]
}
```

Please suggest the schema change and explain how to use the schema.

## How to

## How to run
### CLI
Run the next command, `./gradlew bootRun --args='--spring.profiles.active=app'`, in Terminal.
### IDE
Run OrderApplication with `app` profiles.

## How to send request to gRPC server
1. Please install [gRPCurl](https://github.com/fullstorydev/grpcurl)
2. Set the proper options & parameters. Example commands are blow.
### Create Order
```
$ grpcurl --plaintext -d '{"user_id":"e9a78a73-9b25-4a93-809d-4bd0d2d36dd6", "items":[{"product_id":"test", "price": 100000, "quantity": 20}]}' localhost:8888 kamereo.interview.order.OrderService/CreateOrder
{
"order_id": "ef1d8654-2e56-4ca0-aec7-147a6fe1bf03"
}
```
### Accept / Cancel / Receive
```
$ grpcurl --plaintext -d '{"order_id":"a9f228c0-57b8-42e0-b6e0-8c4984f2a639a"}' localhost:8888 kamereo.interview.order.OrderService/Accept
ERROR:
Code: InvalidArgument
Message: 2
$ grpcurl --plaintext -d '{"order_id":"ef1d8654-2e56-4ca0-aec7-147a6fe1bf03"}' localhost:8888 kamereo.interview.order.OrderService/Accept
{

}
```

### How to update API definition
1. go to `src/main/proto`
2. update proto files
3. run `./gradlew generateProto`
   
## Resources
- [gRPC](https://grpc.io/)
- [gRPCurl](https://github.com/fullstorydev/grpcurl)
- [Spring Boot](https://spring.io/projects/spring-boot)
