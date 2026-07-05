# 정적 wiki(HTML 강의노트 + Markdown)를 nginx로 서빙한다.
# Dokku가 이 Dockerfile을 감지해 빌드·배포한다: git push dokku main
FROM nginx:alpine
COPY deploy/nginx.conf /etc/nginx/conf.d/default.conf
COPY . /usr/share/nginx/html
EXPOSE 80
