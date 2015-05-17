#!/bin/sh
scp -i ~ben/certs/rtb4free_key.pem exchange.html ubuntu@rtb4free.com:XRTB/web
scp -i ~ben/certs/rtb4free_key.pem login.html ubuntu@rtb4free.com:XRTB/web
