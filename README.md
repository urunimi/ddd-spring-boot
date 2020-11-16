
## 프로젝트 실행 방법

### 환경변수 설정

```shell script
> export KAKAO_API_KEY="KakaoAK {{카카오 API Key}}"
> export NAVER_SECRET_KEY="{{네이버 API Secret Key}}"
```

위 환경변수가 세팅되어야 서버 구동 및 테스트가 실행 가능

### 서버 실행

```shell script
> ./gradlew bootRun
```

### API 테스트

[requests.http](https://github.com/urunimi/ddd-spring-boot/blob/main/requests.http) 참고

### 유닛 테스트

```shell script
> ./gradlew test
```

## 부하 대응 방법

### API 자체 부하 혹은 Remote call(Kakao, Naver) 부하가 클 경우

- API 호출 결과를 DB 에 저장해서 캐시로 활용. 적절한 Expire 시간 둠 (Redis 나 DynamoDB 와 같은 TTL 지원 DB 활용)

### DDOS 처럼 하나의 클라이언트에서 많은 요청이 들어오는 경우

- Rate limit 을 활용해서 쓰로틀링 로직을 개발 (IP 혹은 Client 별 Limit 설정)

## Known issues

- API 의 Pagination 이 Query 기반이 아닌 Page index 기준으로 응답을 내리기 때문에 중간에 새로운 데이터가 들어올 경우 데이터의 순서에 문제가 발생.
    - API 응답의 기준을 Place 혹은 Image 의 생성시간(삭제하지 않는다는 가정) 기준 오름차순으로 정렬한다면 문제 없음.
- Naver 의 경우 검색 시작 위치(`start`)가 1이 최대 라서 Pagination 이 불가능 ([Link](https://developers.naver.com/docs/search/local/))

## 사용한 오픈소스

| 패키지 | 사용목적 |
|------|--------|
| com.squareup.retrofit2:retrofit | 외부 API 호출을 위한 Http client 라이브러리 |
| org.junit.jupiter:junit-jupiter | 테스트 케이스 작성을 위한 라이브러리 |
| com.nhaarman.mockitokotlin2:mockito-kotlin | 유닛테스트에서 의존성을 모킹을 위한 라이브러리 |
| com.squareup.okhttp3:mockwebserver | 유닛테스트에서 외부 API 호출을 막고 응답을 모킹하기 위한 라이브러리 |