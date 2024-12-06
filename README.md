# Online-grocery
online grocery allows users to get list of items and prices, to show various discounts on items, and to place order and generate reciept by correctly taking care of discounts.

Endpoints::
1. GET /api/v1/prices: It gets the list of items and prices.
2. GET /api/v1/discount_rules: It gets the list of discount rules applicable to items.
3. POST /place_order: It places order and if successful return receipt of the order with total price after applying discounts if applicable. 
Request/Response Formats::
Request for post is sent in json format. 
sample post request:
 `{
  "orderItems": [
        {
        "name":"bread",
       "quantity": 3,
	"age":3
    },
    {
       "name":"vegetable",
       "weight":200.00
    },
 {
      "name":"Dutch beer",
      "quantity": 6,
       "origin":"Dutch"
 }

  ]
}
`
Response  in json for above request:
Order details:
3 x bread (3 days old): €2.00
6 x Dutch beer: €1.00
Total: €3.00
Error Handling::
1. If bread older than six days added IllegalArgument Exception is thrown.
2. If order with no items is placed NotValidOrder exception is thrown.
3. If any orderItem does not match (by name) with items in inventory ItemNotFound exception is thrown. 

Testing::
1. Various names should be used for testing(case sensitive):bread,vegetable,Dutch beer,German beer, Belgium beer.
2. For bread need to specify age and quantity. 
3. For beer quantity and origin and for vegetable weight(in decimal) should be specified.(refer to sample post request above)
