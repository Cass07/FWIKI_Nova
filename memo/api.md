#### GET, POST
- `/api/sync`
  - 동기 API로 외부 api를 호출해서 그 결과 json을 반환
- `/api/async`
  - 비동기 API로 외부 api를 호출해서 그 결과를 저장하고, 그 결과를 조회할 수 있는 endpoint를 반환
- `/api/async/{id}`
  - `/api/async`로 호출한 비동기 API의 결과를 조회하는 endpoint
  - id는 async api 호출 시 반환된 id


#### 비동기 작업 테스트
- put method
  - asyncResult에 일단 insert하고 id를 반환 (조회용 데이터임)
  - 이후 다른 테이블에 데이터 삽입 (5~10초정도 sleep시키기)
  - 데이터 삽입에 성공하면 asyncResult에 다른 테이블의 id 및 조회 관련 정보를 update
  - id를 통해서 데이터 삽입 여부를 다른 api로 조회 가능하게


#### AsyncApiService의 flux 대충 플로우
- 먼저 asyncResult 테이블에 작업 관련 정보를 저장해서 id를 부여받는다. 이후 저장된 객체를 emit한다
- flatMap으로 받는다
- 내부에서 Mono를 사용해서 또 비동기 작업을 한다
- 비동기 작업 시작 즉시 response 객체를 리턴한다 (작업 진행과 관계없이 바로 response가 나가도록 하기 위해서)
  - 작업을 처리하고 결과를 emit한다
  - 성공했으면 table을 update한다


#### OpenAI를 사용해서 번역 데이터 올리기
- Mono Stream으로 hero 단일 데이터
- flatMap으로 hero당 여러 대사 flux로 변환
- 대사당 Rest API로 번역 요청을 보내고 (parallel로 진행해서 병렬 처리)
  - 통신에 실패했을 경우에는 ? 1회 재시도 후 대체 문구 삽입
  - 대체 문구를 삽입할 경우 에러 리포트를 발행해서 수동으로 수정할 수 있도록 한다
- Map으로 entity 객체로 변환하고
- collectList로 리스트로 변환해서 리턴
- 리턴한 리스트를 벌크로 INSERT 하기 (IO 횟수 줄도록)