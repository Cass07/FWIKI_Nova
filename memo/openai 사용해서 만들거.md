1. batch
   - 실시간으로 처리할 필요가 없으므로 batch 사용
   - 아마도? 제일 처음에만 프롬프트 보내면 나머지는 안써도 될거같?음
   - jsonl 파일을 생성해서 업로드하는 과정 필요
   - batch 결과 또한 jsonl 파일로 제공되는 것으로 보임
   
2. webhook
   - batch 완료 시 웹후크를 발생시켜서 처리 완료된걸 바로 확인할 수 있게 함
   - 웹후크 발생하면 처리 완료된 응답 id를 알려줌
   - 웹훅 엔드포인트 호출 시 보안을 위해 secret을 같이 전송해줌
     - OpenAI SDK를 사용해서 확인할 수도 있고
     - 문서 보고 구현해도 되고
     - https://github.com/standard-webhooks/standard-webhooks/blob/main/spec/standard-webhooks.md#verifying-webhook-authenticity

3. 대충 플로우
   - batch 보내는거
     - 1주일 이상 지난 데이터중에 제일 최신 데이터 n개 묶음
       - 이미 batch 처리 보냈는데 응답이 오지 않은 데이터는 제외해야 함
       - 대사가 일부 추가되서 일부만 미번역인 경우는?? 그 일부만 보내면 될것같음
     - 그 뒤로는 json으로 데이터 정리
     - 파일 업로드 (openai)
     - batch 요청
     - 데이터를 묶고 처리하는 동안의 새로이 추가되는 데이터는 신경쓰지 않음(사용자 경험 문제).
       - 데이터를 보내고 받는 사이에 사용자 데이터가 입력된다면, 이는 받은 데이터를 처리하는 시점에 핸들링하는 것으로 한다
     - 단, 수동으로 배치를 실행할 때 동시에 여러번 실행된다면, 그건 막아야함
       - 배치 시작 시 작업의 데이터를 테이블에 저장하므로, 현재 처리가 왼료되지 않은 작업 데이터가 있다면
         - 작업 데이터만 삽입해서 작업을 예약하자
           - 그렇다면 한번에 3개 이상의 호출이 몰렸다면? 예약된 순서대로 처리하자
           - 그렇다면 아예 배치 시작 시 작업을 예약만 하고 순차적으로 처리하도록 하면 되는거아닌가??
   - 처리되지 않은 batch 데이터 처리
     - batch 최대 24시간까지 지연될수잇음 (그뒤로오면 처리 실패된것)
     - 24시간이 지났는데 성공하지 못한 요청 데이터는 삭제
   - webhook 엔드포인트 실행 시
     - jsonl 파일을 받음
     - 각 json의 데이터 검증
       - 실패시 받은 데이터를 에러 모니터링에 기록
       - exception 발생시키고 slack 메시지 전송
     - 풀어서 work id별로 작업을하는데
       - 요청을 보내고 받는 사이에 손번역 데이터가 추가되었다면?
         - 데이터를 새로 입력하되 반려 데이터로 저장할것
       - 그 이외에 경우에는 그냥 새로 추가하면 문제없음
     - 동기처리하면 오래걸리니까 비동기로 처리하기
     - 각 배치잡의 데이터 셋은 중복되지 않음을 호출 때 보장하긴 하는데... 혹시 중복되면 어캄?

4. 상세 플로우
   - 데이터를 수집해서 batch에 전송하기
     - batch 전송과정의 동시실행을 막기 위한 처리?
       - 사실 mq같은거 써서 큐에 작업요청을 쌓고 하나씩 처리하는게 좋겠죠... 근데 예산상 큐를 안쓴다면
       - BatchInfo 테이블에 작업 예약 데이터를 삽입하고
       - Batch 전송 작업을 수행 중인 스레드에서 이어서 진행하도록 하기
         - 작업이 끝나고 모든 트랜잭션이 종료되면 batchInfo 를 확인한다던가?
         - 근데 이러면 트랜잭션의 단위가 애매해질것같기도 하고 우선은 구현만 해둔뒤에 정책을 생각해볼것
       - 일단 작업도중에 호출하면 실패하는것으로 합시다 그게 제일 최선의 정책인듯함 호출 실패하면 호출한 당사자가 처리해야함
     - 작업 플로우
       - 요청이 시작되면 BatchInfo 데이터를 생성해서 삽입 후 response로 리턴해주고
       - 조건에 맞는 최신 n건의 HeroQuote를 규격에 맞는 json으로 가공하고
       - 각각의 HeroId별로 BatchQuoteInfo를 생성해 list 를 만들고 (테이블에 기록은 아직 안함)
       - 모아서 jsonl 형식으로 File Upload API를 사용해서 파일을 올리고
       - Batch API를 사용해서 배치 작업을 요청하고
       - 받은 batch의 id를 BatchInfo에 기록하고 Batch 요청이 완료되었다고 Status로 업데이트
       - 받은 batch id를 BatchQuoteInfo list에 추가해서 insert
     - 메모
       - 하나의 batch를 만들 때, 여러 개의 heroQuote를 가져와서 각각 Dto로 변환하고, 이걸 Batch API를 위한 JSON 객체로 변환하는 과정을 거쳐야함
       - Batch API용 JSON 객체로 변환하는 작업을 수행하는 주체는 누구여야 하지 ???
   - 완료된 batch 데이터를 가공해서 삽입하기
     - 작업 플로우
       - 작업이 완료된 Batch의 jsonl을 받아서 하나씩 분리하고
         - BatchQuoteResult 테이블에 raw json 데이터 insert -> batch insert?
         - raw json의 유효성 검증 (response 값이 원하는 형식대로 들어왔는지)
           - error가 발생하면 이하의 작업만 안하고, result insertion 은 진행할 것
         - hero id lock 획득
         - 최신 heroQuoteKr id값을 획득
           - 존재하지 않는다면 1로 해서 공개로
           - 존재한다면 +1로 삽입하고 비공개로
         - 각 heroQuoteKr 엔티티를 생성
         - 전부 생성하면 batch insert
         - hero id lock 릴리즈
         - BatchQuoteInfo의 status를 COMPLETED로 갱신
       - 모두 완료되었다면 BatchInfo의 status를 COMPLETED로 갱신
   - 작업할 HeroQuote의 조건
     - HeroQouteKr에 데이터가 없음
     - BatchQouteInfo에 요청중인 데이터가 없음
     - 제일 최신에 등록된 Hero의 순서대로
   - HeroQuoteKr 삽입 시 동시성 문제를 해결하기 위한 플로우
     - 앞에서 api데이터 받아서 가공하고 처리해서 일단 넘겨줌
     - heroQuote 최신 index를 가져옴
     - heroQoute + 최신index+1로 네임드 락을 획득함
       - 못가져오면 가져올 때까지 index에 1씩 더해서 락을 획득하자
     - 최신 index+1로 heroQuoteKr list 객체 만듬
     - batch insert함
     - 획득했던 네임드 락을 릴리즈함
     - 네임드 락 <- php코드에서도 동일한 플로우로 삽입하도록 코드 수정할것
     - 수정에는 필요 없다
     - 조회하고 인서트하는 이 블록을 하나의 트랜잭션으로 묶을 것

5. DB 테이블 설계
   - batch list 요청 정보 테이블 (BatchInfo)
     - 요청의 묶음인 batch에 자체에 관한 정보
     - batch id
     - 요청 시간
     - 상태
       - PENDING : 작업 요청 중
       - REQUESTED : Batch 요청이 완료됨
       - RUNNING : 작업 완료되서 데이터 정리하는중
       - COMPLETED : 완료
       - FAILED : 실패
   - batch 요청 정보 테이블 (BatchQuoteInfo) <- 굳이 필요한가?? 아필요함 요청중복방지필요
     - (batch 내의 각각의 json 요청 정보에 관한 테이블임)
     - BatchInfo id
     - hero id <- jsonl custom id
     - 요청 시간
     - 상태
       - PENDING : 작업 요청 중
       - COMPLETED : 완료
       - FAILED : 실패
   - batch 결과 저장 테이블 (BatchQuoteResult)
     - BatchInfo id
     - hero id
     - 결과 수집 시간
     - json raw data
     - raw 데이터는 로깅만 해도 되지 않을라나?? 일단 테이블에 저장해봄

6. 기타
   - 데이터 처리할 때 flux 사용해서 비동기 처리하고
   - 데이터를 삽입할 때는 flux 전부 처리했을 때 여러개 묶어서 한번에 insert 요청 넣도록 하기

7. file upload
   - json 생성하고 이걸 배열로 만들어서 \n으로 이어붙이면 jsonl이 됨
   - byte배열로 전송해도 된다고함

8. 없는 데이터의 조건
   - kr 데이터가 아예 없음
     - 제출된 데이터가 하나라도 있으면 상관없음
   - kr 데이터가 있으나 신장을 가지고 있으면서 신장 데이터의 kr 데이터가 없음
     - 신장테이블에 정보가 있으면서 kind = "Atk_R" 데이터가 없는거

9. ㄹㅇ 기타
   - MSA스럽게 만드려면 SQS같은 Queue를 통해서 id list를 받고 -> 이 프로젝트에서 batch job을 핸들링하고 -> 받은 데이터를 가공해서 heroQuoteKr을 handling하는 또 다른 MSA를 호출해야 헸겠지만
   - 소규모 프로젝트라 돈문제로 하나로 굴릴거기때문에 하나의 애플리케이션에서 전부 처리하되 해당 단계를 나눠서 구현험으로서 쉽게 확장할 수 있도록 하자
   - 객체는 각자의 가공 및 상태 변화만 담당하기
   - OpenAI 호출하고 받는 작업은 별도의 객체로 분리하기

10. quote kr version 관리할 때 네임드 락킹 
   - 버전을 조회하고 -> insert 완료할 때까지 네임드 락을 지정함
   - 사용자와 api 삽입이 동시에 일어날 때 버저닝 충돌을 막음