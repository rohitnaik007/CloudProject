availabilityZone="us-east-1a"
read -p "Enter your VPC STACK_NAME " name
vpcName="$name-csye6225-vpc"
gatewayName="$name-csye6225-InternetGateway"
routeTableName="$name-csye6225-public-route-table"
vpcCidrBlock="10.0.0.0/16"


. "./$vpcName.txt"
vpc=$(echo $VPC_ID)
ig=$(echo $IG_ID)
routeTableId=$(echo $ROUTE_TABLE_ID)
subnet1=$(echo $SUBNET1)
subnet2=$(echo $SUBNET2)

echo "Deleting Subnets ...."

aws ec2 delete-subnet --subnet-id $subnet1
aws ec2 delete-subnet --subnet-id $subnet2

echo "Subnets Removed"
printf "\n"

echo "Deleting custom route table ...."
aws ec2 delete-route-table --route-table-id $routeTableId

echo "Detaching internet gateway ...."
aws ec2 detach-internet-gateway --internet-gateway-id $ig --vpc-id $vpc

echo "Deleting internet gateway ...."
aws ec2 delete-internet-gateway --internet-gateway-id $ig

echo "Deleting VPC ...."
aws ec2 delete-vpc --vpc-id $vpc

printf "\n"

echo "Stack cleared! All operations successful. Have a nice day!"