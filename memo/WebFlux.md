#### Reactive Programming
- 비동기적 프로세스 파이프라인을 위한 패러다임
- 햠수형 프로그래밍과 유사한 `선언적 코드`를 사용함
  - 선언적 코드란?? 코드에서는 어떤 일을 수행해야 할지만 표현하고, 실제 작업은 추상화되어 감춰저 있다는?? 듯
- 리액티브 프로그래밍은 event 기반으로, 작업이 완료되면 그 이벤트를 subscriber에게 push하는 방식으로 동작함
- 비동기(asynchronous)와 논블로킹 (non-blocking)을 중점으로 둠

#### WebFlux
- 비동기 논블로킹
- 이벤트 루프를 돌리고 요청이 발생하면 해당 요청을 처리하는 핸들러에게 위임
- 처리가 완료되면 콜백 메소드에게 전달해서 응답을 반환
- 논블로킹이기 때문에 MVC랑은 달리 요청을 처리하는 동안 다른 요청을 처리할 수 있음
  - 그래서 하나의 요청을 처음부터 끝까지 스레드에 묶어두지 않기 때문에 동시처리에 유리
- 호출이 많은 경우에 적합
  - MSA
- WebFlux를 사용해서 제대로 비동기적 서비스를 운영할때는 Netty등의 논블로킹 서버를 사용하라고함

##### Spring WebFlux
- Spring MVC는 서블릿 기반이라 블로킹으로 동작하는데
- Spring WebFlux는 리액티브 프로그래밍을 지원하는 비동기 논블로킹 웹 프레임워크
  - 그래서 기본 서버 엔진이 Netty (s리액티브 스트림즈를 지원한다고 함)
- 데이터 계층 액세스도 논 블로킹으로 할 수 있도록 Spring Data R2DBC를 지원함
  - 논 블로킹 IO를 지원하는 NoSQL 모듈을 사용

##### 함수형 스타일을 사용함으로서 얻는 장점
- 모든 웹 요청 처리 작업을 명시적인 코드로 작성
- 함수 조합을 통해 복잡한 작업을 간단하게 표현, 추상화에 유리함
- 테스트 작성에 편리
  - MVC는 Controller 레이어 테스트를 위해서는 MockMVC를 사용해서 웹 요청을 모킹할 수밖에 없는데
  - 웹플럭스는 요청 매핑과 리턴 값 처리까지 단위 테스트로 작성하기 용이하다고 한다
    - 이유는???? 몰라 더 찾아봐야지

#### Reactive Stream 대충 구조
- publisher
  - 순차적 데이터 스트림을 생성
  - subscriber의 구독을 받기 위한 subscribe 메소드를 제공
- Subscriber
  - 순차적 데이터를 받아서 처리하는 컴포넌트
  - onNext, onError, onComplete 메소드를 구현해서 데이터를 처리
- Subscription
  - publisher가 생성한 구독에 대한 정보를 관리하는 컴포넌트
  - Publisher와 Subscriber 사이의 연결을 관리
  - Subscriber가 Publisher에게 요청을 보내는 메소드 제공
- Processor
  - Publisher와 Subscriber의 역할을 모두 수행하는 컴포넌트 (미들웨어)

    
##### flow
1. Subscriber가 Publisher에게 구독 요청
2. Publisher는 Subscriber에게 Subscription을 전달
   - Subscription이 Publisher와 Subscriber 사이의 연결을 관리
3. Subscriber는 Subscription을 통해 Publisher에게 데이터 요청 (Subscription을 통해서만 요청하게된다)
4. Publisher는 Subscriber에게 Subscription을 통해 시그널을 전달


#### Flux? Mono?
- Reactive Stream에서 Publisher 인터페이스를 구현한, 퍼블리셔 역할을 수행하는 객체
- Publisher?
  - 리액티브 스트림을 구현하는?? 인터페이스?
- 제일 큰 차이점 
  - Flux는 0개 이상의 데이터를 발행할수있는데
  - Mono는 0~1개만 발행가능