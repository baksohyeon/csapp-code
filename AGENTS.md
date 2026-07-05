# AGENTS.md

이 레포에서 작업하는 에이전트를 위한 규칙.

## 프롬프트 작성

- 프롬프트 작성을 요청받으면 **GPT 5.5 official prompt guide** 기준으로 작성한다.

## 레포 구조

이 레포는 CSAPP(Computer Systems: A Programmer's Perspective) 스터디 기록이다.
`wiki/`가 knowledge base root이고, filesystem이 single source of truth다.

- `wiki/index.md` — 전체 목차. 새 문서를 추가하면 여기에도 링크를 추가한다.
- `wiki/lectures/<year>/` — 강의노트. 스터디는 매주 일요일 오전 10시에 열리지만 불참하는
  주도 있으므로, 주차가 빠져 있어도 정상이다 (연속성을 가정하지 않는다). HTML 원본이면 같은
  이름의 `.md` wrapper 쌍으로 두고, 처음부터 Markdown으로 쓴 강의노트는 `.md` 단독으로 둔다.
- `wiki/notes/` — 개별 학습 노트, 학습 로그.
- `wiki/topics/` — 토픽 노드. 여러 노트/강의가 공유하는 개념 단위.
- `wiki/type/` — 타입 렌즈 문서 (`lecture`, `note`, `topic`, `example`).
- `wiki/assets/<note-id>/` — 노트별 이미지 등 첨부 파일.
- `examples/` — 실습 코드. 노트가 참조하는 소스와 산출물.

## Wiki 작성 규칙

1. **모든 wiki Markdown은 YAML frontmatter를 사용한다.** 필드: `type`, `status`, `date`,
   `topics`, `belongs_to`, `related_to`, `has`, `source_url`, `artifact_path`.
   해당 없는 필드는 생략한다.
2. **`type`은 강제 스키마가 아니라 탐색 렌즈다.** 기본값은 `Lecture`, `Note`, `Topic`,
   `Example`. 필요하면 새 타입을 추가하되 `wiki/type/`에 렌즈 문서를 함께 만든다.
3. **관계는 wikilink로 표현한다.** `[[cache-locality]]`처럼 대상 파일명(확장자 제외)으로
   링크한다. 아직 없는 문서를 가리키는 wikilink는 오류가 아니라 "나중에 쓸 문서" 표시다.
4. **HTML 강의노트에는 frontmatter를 직접 넣지 않는다.** 같은 이름의 `.md` wrapper가
   날짜, 토픽, 원본 HTML 경로(`artifact_path`)를 소유한다. 처음부터 Markdown으로 작성한
   강의노트는 wrapper 없이 그 `.md`가 직접 frontmatter를 가진다.
5. **강의노트의 `date`는 노트 생성 일자다.** 파일명 날짜와 frontmatter `date`를 일치시킨다.
   해당 스터디 세션 날짜(보통 일요일)가 생성 일자와 다르면 본문에 세션 날짜를 명시한다.
   불참 등으로 빠진 주차가 있어도 채우지 않는다 — 있는 노트만 기록한다.
5. **`.o` 같은 빌드 산출물은 실습 노트가 직접 참조하는 경우에만 추적한다.** 해당 노트에
   CPU/플랫폼 의존 산출물이라는 점과 재생성 명령을 함께 적는다.
6. 날짜는 상대 표현 대신 절대 날짜(`YYYY-MM-DD`)로 적는다.

## 파일 명명

- 강의노트: `YYYY-MM-DD-<slug>.{md,html}`
- 노트: `<id>-<slug>.md` 또는 `YYYY-MM-DD-<slug>.md`
- 토픽: `<slug>.md` (kebab-case)
- 실습: `examples/<id>-<slug>/`
