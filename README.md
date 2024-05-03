# Online-grocery
Assignment for online grocery by Nidhi

Endpoints::
GET /api/v1/prices: It gets the list of items and prices.
GET /api/v1/discount_rules: It gets the list of discount rules applicable to items.
POST /place_order: It places order and if successful return receipt of the order with total price after applying discounts if applicable. 
Request/Response Formats::
Request for post is sent in json format. 
sample post request:
 {
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
Response  in json for above request:
Order details:
3 x bread (3 days old): €2.00
6 x Dutch beer: €1.00
Total: €3.00
Error Handling::
Api to place order handles error scenarios like if bread older than six days added or if order with no items is placed.If any orderItem does not match (by name) with items in inventory ItemNotFound exception is thrown. 
Testing::
Various names can be used for testing(case sensitive):bread,vegetable,Dutch beer,German beer, Belgium beer.
for bread need to specify age and quantity. for beer quantity and origin and for vegetable weight(floating point) should be specified(refer to sample post request above)
