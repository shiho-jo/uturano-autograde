FROM nginx:latest
WORKDIR /usr/share/nginx/html

COPY frontend/pages/ /usr/share/nginx/html
COPY frontend/asset/ /usr/share/nginx/html/asset
COPY frontend/config/cert.pem /etc/nginx/cert.pem
COPY frontend/config/cert.key /etc/nginx/cert.key
COPY frontend/config/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
EXPOSE 443