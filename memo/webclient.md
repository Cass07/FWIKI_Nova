#### 대충 구조
- WebFlux의 WebClient를 사용해서 비동기/non-blocking 방식으로 http request handling

#### Spring Boot에서 WebClient 쓸 떄
- WebClient를 사용하기 위해서는 의존성 추가가 필요
  - build.gradle에 다음 의존성 추가
    ```groovy
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    ```
- WebClient 빈을 생성
  - WebClient.Builder를 사용해서 WebClient 빈을 생성
  - 예시:
    ```java
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
    ```
    
- WebClient를 사용해서 Impl 클래스를 만들어서 이걸 상속받아서 구현해야 하나???
  - 하나의 webclient로 여러 API를 위한 클래스를 생성하고자함
  - base url을 클래스별로 지정하고 클래스마다 별개의 json token 생성규칙을 적용하고자함???
    - 그러려면 secret, base url 등의 값을 가지도록 해야 하는데 상태를 가지게 하는 게 맞나???


#### Sample business logic
- Spring Boot Rest API interface
  - Rest API로 request하면, 다른 API를 호출해서 그 결과를 반환
  - status code, timeout over 의 예외 처리

- status code 예외 처리
  - 200: 정상
  - 40x : 클라이언트의 에러 (rollback 안함)
  - 50x : 상대 서버의 에러 (rollback 함)
- timeout over
  - 지정 타임아웃 시간을 두고 그 시간동안 reponse를 받지 못했을 때
  - 근데 타임아웃일 땐 상대가 성공했는지 실패했는지 알 수가 없음??
  - 일단은 상대 서버의 timeout 시간보다 여유롭게 줘서, 상대 서버가 응답을 못했을 때만 timeout으로 처리하는게 제일 좋을 것 같음
    - 해당 작업을 기록해놓고 직접 성공 여부를 조회해서 이후 처리하는게 맞지 않나싶음??
      - ex) post 요청을 보내서 데이터를 등록할 때
        - timeout threshold를 넘겨서 실패 처리했다면 해당 post 요청을 기록
        - 이후 조회 api를 통해서 해당 post 요청의 성공 여부를 조회
          - 성공했다면 post 이후 비즈니스 로직을 처리
          - 실패했다면 재시도?

#### 샘플 API 종류
- 그냥 동기적 API
  - request를 보내면 그 조건에 맞게 외부 API들을 호출하고 결과가 전부 모이면 response 반환
  - 한 비즈니스 로직에서 여러 api를 호출한다면 걔내들은 비동기 & 논블로킹으로 처리할것임
- 논 블로킹 API
  - request를 보내면 그 처리 결과를 확인할 수 있는 endpoint를 즉시 반환
  - 내부적으로는 알아서 비즈니스 로직을 처리해서 결과를 저장

#### 외부 API의 호출 빈도를 제한하고 싶다면???
- 모든 api 호출에 대해서 프록시 패턴 등을 사용해서 전/후에 호출 빈도수를 제어하는 코드를 삽입해도 될듯???
  - concurrency하게 시간당 호출 수를 기록해서 제한 수를 초과할 경우에는 exception을 발생시키면 간단하게 호출 수 제한이 가능
- Resilience4j 를 사용해보라고함??

#### 외부 API 호출 시의 테스트 전략
- 다른 비즈니스 로직때는 그냥 mocking해서 테스트하면 되고
- API호출부 자체는?