# 정적 wiki(HTML 강의노트 + Markdown)를 nginx로 서빙한다.
# Dokku가 이 Dockerfile을 감지해 빌드·배포한다: git push dokku main
FROM nginx:alpine
COPY deploy/nginx.conf /etc/nginx/conf.d/default.conf
COPY . /usr/share/nginx/html
# 레포의 학습 문서 index.html을 유지하고 배포 설정 파일을 숨긴다.
RUN rm -rf /usr/share/nginx/html/Dockerfile \
           /usr/share/nginx/html/.dockerignore \
           /usr/share/nginx/html/deploy
EXPOSE 80
