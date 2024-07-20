# E-Commerce-MSA

## 🚍프로젝트 개요


온라인 굿즈(Goods) 판매 사이트입니다. 특정 시간에 상품을 한정적으로 구매할 수 있는 서비스를 제공합니다.

### 📆 개발 기간


 **2024.06.19 - 2024.07.17 (4주)**

### 📌 프로젝트 Docs

### [E-Commerce Docs (GitBook)](https://app.gitbook.com/o/o7ZlP7rkRet0OIjuerME/s/UcA06eoZuvIpbSm9BmTn/)

---

## 📲 Docker Execution

```bash
docker-compose up -d
```

## 📝 API 문서

### [E-Commerce API (Postman)](https://documenter.getpostman.com/view/29105938/2sA3kUFM8D#intro)

---
## 🌈 시스템 아키텍처
![image](https://github.com/user-attachments/assets/b142021a-4e2d-467c-b1b2-e5bf5fb7dd20)


---
## 🛒 주요 기능


1. **회원 기능**
- 사용자는 **이메일 인증**을 과정을 거쳐 계정을 생성할 수 있습니다.
- 마이페이지를 통해 **회원 정보를 수정**하고 **주문 내역**을 조회할 수 있습니다.
2. **상품 기능**
- 검색어, 카테고리에 따른 조회 결과를 **필터링**할 수 있습니다.
- **관리자**는 상품을 **등록, 수정, 삭제**할 수 있습니다.
3. **장바구니**
- 장바구니에 담긴 상품을 확인하고 항목을 변경할 수 있습니다.
- 총 금액 및 개수를 확인할 수 있습니다.
4. **결제 및 주문**
- 구매 가능한 상품에 대해 주문 및 장바구니에 담을 수 있습니다.
- 주문 내역에서 **주문 취소 및 반품** 신청을 할 수 있습니다.
5. **선착순 구매**
- **특정 시간**에 판매되는 한정된 상품을 **선착순으로 구매**할 수 있습니다.

## 🛠 기술 스택
### 💻 Backend
- Spring Boot
- Spring Security
- Spring Data JPA 
- Kafka
- Docker
- PostgreSQL
- Redis
