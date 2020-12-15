# ddd-spring-boot



## 실행 방법

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

## 사용한 오픈소스

| 패키지 | 사용목적 |
|------|--------|
| com.squareup.retrofit2:retrofit | 외부 API 호출을 위한 Http client 라이브러리 |
| org.junit.jupiter:junit-jupiter | 테스트 케이스 작성을 위한 라이브러리 |
| com.nhaarman.mockitokotlin2:mockito-kotlin | 유닛테스트에서 의존성을 모킹을 위한 라이브러리 |
| com.squareup.okhttp3:mockwebserver | 유닛테스트에서 외부 API 호출을 막고 응답을 모킹하기 위한 라이브러리 |