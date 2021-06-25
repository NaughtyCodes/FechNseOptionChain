# FetchNseOptionChain
Education and learning purpose project to read options chain data from NSE website

## Project Structure

![image](https://user-images.githubusercontent.com/85377881/120890353-c9641f80-c61f-11eb-9f23-aac89fa066a0.png)

## UI

![image](https://user-images.githubusercontent.com/19818842/121783551-df984f80-cbcc-11eb-8eac-fa623db30847.png)


http://hostname:port/opt/by/price/[symbol]/[Strik-Price]
  
  eg: http://localhost:8080/opt/by/price/ITC/190
  
![image](https://user-images.githubusercontent.com/19818842/120807617-c6a4f400-c565-11eb-9e0b-a3b7ce849734.png)
  
http://hostname:port/opt/by/expiry/ITC/[Expiry-Month]
  
  eg: http://localhost:8080/opt/by/expiry/ITC/MAY

![image](https://user-images.githubusercontent.com/19818842/120808641-e38df700-c566-11eb-916c-45bf10abc4f5.png)

"http://hostname:port/opt/by/expiry/all/[Expiry-Month]"
  
  eg: http://localhost:8080/opt/by/expiry/all/[ month ]?gitFlag=false
  
  To get all the NSE stocks option chain details by expiry date.

http://hostname:port/opt/by/expiry/all/[ Month ]/[ year ]?gitFlag=[ true/false ]
  
  eg: http://localhost:8080/opt/by/expiry/all/jun/2021?gitFlag=false
  
  To get all the NSE stocks option chain details using async method by expiry date.

## Swagger Url

  http://localhost:8080/swagger-ui.html#/options-web-controller
  
  ![image](https://user-images.githubusercontent.com/19818842/120893086-e8b67900-c62e-11eb-8f28-34df9e6fab8f.png)

