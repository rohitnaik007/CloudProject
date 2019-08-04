availabilityZone="us-east-1a"
read -p "Enter your VPC STACK_NAME " name
vpcName="$name-csye6225-vpc"
gatewayName="$name-csye6225-InternetGateway"
routeTableName="$name-csye6225-public-route-table"
vpcCidrBlock="10.0.0.0/16"

destinationCidrBlock="0.0.0.0/0"
printf "\n"
echo "Creating VPC ...."
printf "\n"
#create vpc with cidr block /16d
aws_response1=$(aws ec2 create-vpc --cidr-block "$vpcCidrBlock")

vpcId=$(echo -e "$aws_response1" | grep VpcId | awk '{print $2}' | tr -d '"' | tr -d ',')

destdir="$vpcName".txt
echo "VPC_ID=$vpcId" > "$destdir"

# Naming the vpc
aws ec2 create-tags --resources "$vpcId" --tags Key=Name,Value="$vpcName"

if [ $? -eq 0 ]; then
	echo "VPC Created and Tagged"
	echo "Creating Subnet ...."
else
	echo "VPC Creation Failed"
fi
printf "\n"


# Create a subnet
aws_response_subnet1=$(aws ec2 create-subnet --vpc-id $vpcId --cidr-block 10.0.1.0/24)
subnetId1=$(echo -e "$aws_response_subnet1" | grep SubnetId | awk '{print $2}' | tr -d '"' | tr -d ',')

# Create a another subnet
aws_response_subnet2=$(aws ec2 create-subnet --vpc-id $vpcId --cidr-block 10.0.0.0/24)
subnetId2=$(echo -e "$aws_response_subnet2" | grep SubnetId | awk '{print $2}' | tr -d '"' | tr -d ',')

if [ $? -eq 0 ]; then
	echo "Subnets created"
	echo "Creating Internet Gateway ...."
else
	echo "Subnet Creation Failed"
fi
printf "\n"
# Make one subnet public by attaching internet gateway to VPC
aws_response2=$(aws ec2 create-internet-gateway)

internetGateway=$(echo -e $aws_response2 | grep InternetGatewayId | awk '{print $10}' | tr -d '"' | tr -d ',')
if [ $? -eq 0 ]; then
	echo "Internet Gateway created ...."
	echo "Attaching subnet to  Internet Gateway ...."
else
	echo "Gateway Creation Failed"
fi
printf "\n"
aws ec2 attach-internet-gateway --vpc-id $vpcId --internet-gateway-id $internetGateway
if [ $? -eq 0 ]; then
	echo "Subnet attached to IG"
	echo "Creating new route table ...."
else
	echo "Subnet Internet Gateway attachment Failed"
fi
printf "\n"

aws_response3=$(aws ec2 create-route-table --vpc-id $vpcId)
routTableID=$(echo -e $aws_response3 | grep RouteTableId | awk '{print $8}' | tr -d '"' | tr -d ',')

echo "SUBNET1=$subnetId1" >> "$destdir"
echo "SUBNET2=$subnetId2" >> "$destdir"
echo "IG_ID=$internetGateway" >> "$destdir"
echo "ROUTE_TABLE_ID=$routTableID" >> "$destdir"

# Create a route in the route table that points all traffic (0.0.0.0/0) to the Internet gateway.

if [ $? -eq 0 ]; then
	echo "Route table created"
	echo "Creating new route ...."
else
	echo "Route table creation failed"
fi


aws ec2 create-route --route-table-id $routTableID --destination-cidr-block 0.0.0.0/0 --gateway-id $internetGateway
aws ec2 describe-route-tables --route-table-id $routTableID
aws_response4=$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$vpcId" --query 'Subnets[*].{ID:SubnetId}')
subnetId=$(echo -e $aws_response4 | grep ID | awk '{print $5}' | tr -d '"' | tr -d ',')

# Set first subnet as default public ip
aws ec2 associate-route-table --subnet-id $subnetId --route-table-id $routTableID
aws_response5=$(aws ec2 describe-subnets)
echo $aws_response5

if [ $? -eq 0 ]; then
	echo "Route table created"
	printf "\n"
	echo "--------------  All operations successful. Have a good day!  --------------"
else
	echo "Route table creation failed"
fi
printf "\n"
