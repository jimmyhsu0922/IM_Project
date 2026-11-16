# 校園小神通

國小班級與社團的正向聊天 App。學生只能從句庫點選句子發言，老師可以自由輸入廣播、設定公告、查看互動統計，管理員在後台管理帳號、班級社團與句庫。

## 系統架構

```
Android App（Kotlin + Jetpack Compose）
├─ ui/       畫面與 ViewModel：auth 登入、student 學生、teacher 老師、admin 管理後台
├─ domain/   資料模型：AppUser、Room、ChatMessage、VocabEntry、Role、GradeBand
└─ data/     資料存取：Auth / User / School / Room / Vocabulary Repository
        │
        ▼
Firebase
├─ Authentication   帳號與密碼（學號轉成「學號@xiaoshentong.example.com」）
└─ Firestore        其餘資料，由 firestore.rules 控管權限
```

## 資料庫（Firestore）

| 集合 | 文件代碼 | 欄位 |
|---|---|---|
| `users` | Authentication 的 uid | account、realName、role（student / teacher / admin）、grade、classId、clubId、active |
| `classes` | 例 `class-101` | name、grade、schoolYear |
| `clubs` | 例 `club-guitar` | name |
| `rooms` | 與班級或社團代碼相同 | name、type（class / club）、announcement（text、senderName、messageId） |
| `rooms/{id}/messages` | 自動產生 | text、senderId、senderName、senderRole、vocabId、category、timestamp、edited |
| `vocabulary` | 例 `lower_1` | text、category（1～7）、gradeBand（lower / middle / upper）、suggestions、teacherSuggestions、caution、enabled、order |

## 句庫

三個年段各 50 句，分成七類：問候與日常、回應與確認、讚美與肯定、感謝與道歉、關心與鼓勵、合作與團隊、求助與師生。
預設內容在 `data/seed/DefaultVocabulary.kt`，由管理員在後台「句庫」分頁按「匯入預設句庫」寫進資料庫。

- `suggestions`：同學收到這句時，推薦給同學的回應。
- `teacherSuggestions`：老師收到這句時，推薦給老師的回應。
- `caution`：這句容易被拿來反諷，後台會標示。

## 權限（firestore.rules）

- 學生、老師只能讀寫自己班級與社團的聊天室。
- 學生送出的文字必須是句庫裡的句子，或該句子的推薦回應。
- 老師可以自由發言、編輯自己的訊息、設定或取消公告。
- 只有管理員能管理帳號、班級社團與句庫。
- 被停用的帳號讀不到任何資料。

## 第一次設定（Firebase 主控台，專案 im-project-c3ab4）

1. **Authentication → 登入方式**：啟用「電子郵件／密碼」。
2. **Authentication → 使用者 → 新增使用者**：電子郵件填 `admin@xiaoshentong.example.com`，密碼自訂（至少 6 碼）。
3. **Firestore Database → 規則**：貼上本專案根目錄 `firestore.rules` 的全部內容，按「發布」。
4. 在 App 用帳號 `admin` 和剛才的密碼登入，進入管理後台。
5. 到「系統」分頁按「建立示範資料」。

舊版的 `users`、`messages`、`announcements` 資料不再使用。舊 `users` 文件含明碼密碼，建議在主控台刪除。

## 示範帳號（密碼皆為 demo1234）

| 身分 | 帳號 | 班級 |
|---|---|---|
| 老師 | T101、T301、T501 | 一年一班、三年一班、五年一班 |
| 學生 | 10101～10104 | 一年一班（低年級句庫） |
| 學生 | 30101～30104 | 三年一班（中年級句庫） |
| 學生 | 50101～50104 | 五年一班（高年級句庫） |

T101 另帶吉他社、T301 另帶熱音社；學生座號單數在吉他社、雙數在熱音社。

## 已知限制

- 管理員無法在 App 內重設別人的密碼或刪除帳號，需到 Firebase 主控台操作；App 內可以「停用」帳號。
- 互動統計以聊天室最近 300 則訊息計算。
- 套件代碼仍是 `com.example.improject`，僅供校內安裝，不能上架 Google Play。
