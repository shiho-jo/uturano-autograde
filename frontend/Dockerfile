# Frontend: Network
FROM nginx:latest
WORKDIR /usr/share/nginx/html

COPY ./pages/ /usr/share/nginx/html
COPY ./asset/ /usr/share/nginx/html/asset
COPY ./config/cert.pem /etc/nginx/cert.pem
COPY ./config/cert.key /etc/nginx/cert.key
COPY ./config/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
EXPOSE 443


# Frontend: Test
FROM node:18 as test-env
WORKDIR /usr/src/app

COPY ./package*.json ./
RUN npm install
RUN npm install --save-dev jest jest-environment-jsdom

ENV PATH="/usr/src/app/node_modules/.bin:$PATH"

COPY ./ ./

CMD ["npm", "test"]
