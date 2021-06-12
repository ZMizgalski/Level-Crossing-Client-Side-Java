# Getting Started
If you want to run this on rspi change sourceCompatibility = '11' from ''16

# To work properly you need to install:
sudo apt install -y gpac

# To add config file:
java -jar app.jar --spring.config.location=file:<application.properties>

# Default Config file: (application.properties)
server.port= 8081
motor1.pin = 0        # Default 0
motor2.pin = 1        # Default 1
motor3.pin = 2        # Default 2
motor4.pin = 3        # Default 3
motor.delay = .5      # Default .5f
open.pwm = .25        # Default .25f
close.pwm = .5        # Default .5f
main-server.ip = 192.168.1.41
main-server.port = 8080