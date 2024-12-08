FROM nginx:latest

COPY pages/ /usr/share/nginx/html
COPY asset/ /usr/share/nginx/html/asset
COPY config/cert.pem /etc/nginx/cert.pem
COPY config/cert.key /etc/nginx/cert.key
COPY config/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
EXPOSE 443