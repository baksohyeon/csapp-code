---
type: Type
status: living
---

# Lecture

스터디(매주 일요일 오전 10시, 불참하는 주도 있음)에서 쌓이는 강의노트. 주차 누락은
정상이며 연속성을 가정하지 않는다. 형태는 두 가지다:

- **HTML 원본 + `.md` wrapper 쌍** — 1차 자료는 HTML이고, 같은 이름의 wrapper가
  frontmatter(날짜, 토픽, `artifact_path`)를 소유한다.
- **Markdown 단독** — 처음부터 Markdown으로 쓴 강의노트는 wrapper 없이 그 `.md`가
  직접 frontmatter를 가진다.

## 규칙

- 위치: `wiki/lectures/<year>/YYYY-MM-DD-<slug>.md` (+ HTML 원본이 있으면 같은 이름의 `.html`)
- 파일명과 frontmatter의 `date`는 노트 생성 일자로 일치시킨다. 스터디 세션 날짜
  (일요일)와 생성 일자가 다르면 본문에 세션 날짜를 명시한다.
- HTML에는 frontmatter를 넣지 않는다. 메타데이터는 wrapper `.md`에만 둔다.
- wrapper는 강의 요약과 관련 토픽 wikilink를 담는다.

## 문서

- [[2026-07-05-csapp-ch6-memory-hierarchy]]
