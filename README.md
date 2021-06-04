# FetchNseOptionChain
Education and learning purpose project to read options chain data from NSE website

http://hostname:port/opt/by/price/[symbol]/[Strik-Price]
  
  eg: http://localhost:8080/opt/by/price/ITC/190
  
![image](https://user-images.githubusercontent.com/19818842/120807617-c6a4f400-c565-11eb-9e0b-a3b7ce849734.png)
  
http://hostname:port/opt/by/expiry/ITC/[Expiry-Date]
  
  eg: http://localhost:8080/opt/by/expiry/ITC/27MAY2021

![image](https://user-images.githubusercontent.com/19818842/120807935-213e5000-c566-11eb-8742-fff0673eddfe.png)

"http://hostname:port/opt/by/expiry/all/[Expiry-Date]"
  
  eg: http://localhost:8080/opt/by/expiry/all/27MAY2021
  
  To get all the NSE stocks option chain details by expiry date.

http://hostname:port/opt/by/async/all/[Expiry-Date]
  
  eg: http://localhost:8080/opt/by/async/all/27MAY2021
  
  To get all the NSE stocks option chain details using async method by expiry date.
