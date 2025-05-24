# 🥗 Last Fresh -사용자 맞춤형 레시피 추천 및 식재료 관리 애플리케이션

<img src="https://img.shields.io/badge/Platform-Android-green?style=flat-square" /> <img src="https://img.shields.io/badge/Language-Kotlin-blue?style=flat-square" /> <img src="https://img.shields.io/badge/Backend-Firebase-yellow?style=flat-square" />

> **Last Fresh**는 냉장고 속 재료를 기반으로 사용자의 건강 상태에 맞춘 맞춤형 레시피를 추천하고, 유통기한 알림 및 재료 관리를 도와주는 Android 애플리케이션입니다.



---

## 📌 프로젝트 개요

- **주제**: 냉장고 속 식재료를 버리지 않고 활용할 수 있도록, 유통기한 관리와 맞춤형 레시피 추천 기능을 결합한 스마트한 식생활 관리 앱 개발
- **타겟 사용자**:
  - 재료 유통기한을 효율적으로 관리하고 싶은 사용자
  - 냉장고 속 재료로 건강한 레시피를 추천받고 싶은 사용자
  - 외식 대신 집밥을 선호하는 1인 가구 및 밀프렙(MEAL PREP) 사용자

---

## 🛠 기술 스택

| 항목         | 내용                                                              |
|--------------|-------------------------------------------------------------------|
| 프론트엔드   | Kotlin (Android Studio)                                           |
| 백엔드       | Firebase, Flask                                                   |
| 외부 API     | 네이버 CLOVA OCR API, 식약처 레시피 API, 네이버 쇼핑 API         |
| 협업 도구    | GitHub, Figma                                                     |

---

## 💡 주요 기능

### 🔍 1. OCR 기반 영수증 인식
- 종이 또는 전자 영수증 촬영 → CLOVA OCR로 재료명 자동 인식
- 네이버 쇼핑 API로 식재료/비식품 분류
- 재료 자동 저장

### 📋 2. 냉장고 재료 목록 관리
- 재료명, 수량, 유통기한, 보관 상태 입력 및 편집
- Firebase에 실시간 저장 및 수정

### 🧠 3. 사용자 맞춤 레시피 추천
- 나이, 성별, 키, 체중 기반 기초대사량(BMR) 계산
- 원하는 재료 기반으로 하루 섭취 열량에 맞춘 건강 레시피 추천

### 🌟 4. 즐겨찾기 기능
- 마음에 드는 레시피를 저장 후 언제든지 재확인 가능

### 🔔 5. 유통기한 푸시 알림
- 당일 유통기한이 도래한 재료를 푸시로 알림

---

## 📱 주요 화면 미리보기

> UI/UX는 Figma로 설계되었으며, 사용자의 흐름에 맞춘 직관적인 구성


![주요 화면 구성 전체보기](screenshots/Last Fresh_화면구성.jpg)

---

## 🧪 데이터 분류 성능

- OCR 분류 정확도: **91.67%**
- 오분류율: **8.33%**
- 신뢰수준: **95%**, 오차범위 ±5%
- 샘플 수: 384개 (대형마트 영수증 기준 샘플링)

---

## 🗓 프로젝트 타임라인

| 기간 | 주요 작업 내용 |
|------|----------------|
| 1~2주차 | 주제 선정, 기술 조사, API 탐색 |
| 3~5주차 | UI/UX 설계, Firebase 연동 |
| 6~8주차 | 레시피 추천 알고리즘, OCR 기능 개발 |
| 9~10주차 | 디버깅 및 기능 고도화, 졸업작품 발표 준비 |

---

## 📄 Library

- [NAVER CLOVA OCR](https://guide.ncloud-docs.com/docs/ko/clovaocr-overview)
- [firebase](https://console.firebase.google.com/u/0/?hl=ko)
- [NAVER 쇼핑 API](https://developers.naver.com/docs/serviceapi/search/shopping/shopping.md)
- [식약처_조리식품레시피DB](https://www.foodsafetykorea.go.kr/api/openApiInfo.do?menu_grp=MENU_GRP31&menu_no=661&show_cnt=10&start_idx=1&svc_no=COOKRCP01)
