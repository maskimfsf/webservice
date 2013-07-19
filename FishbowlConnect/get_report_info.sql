select po.id as "po.id",
num, 
remittoname, 
remitaddress, 
remitcity, 
remitzip, 
buyer, 
shiptoname, 
shiptoaddress, 
shiptocity, 
shiptozip, 
dateissued, 
shipterms.name as "shipterms.name",
carrier.name as "carrier.name", 
paymentterms.name as "paymentterms.name", 
fobpoint.name as "fobpoint.name"

from po 
join customset on po.id = customset.recordid 
join shipterms on shipterms.id = po.shiptermsid 
join carrier on carrier.id = po.carrierid 
join paymentterms on paymentterms.id = po.paymenttermsid 
join fobpoint on po.fobpointid = fobpoint.id

where customset.info = 'Waiting for Approval'; 