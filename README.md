# Lab 1 Starter: Booking Service

A small room-booking service. Users book rooms for time intervals, and if a room is
taken they land on a waitlist. It is the codebase you work in for Lab 1.

**Read `ARCHITECTURE.md` first.** It maps the three layers (domain / service / repo)
so you do not have to cold-read every file.

## Build and test

```
mvn test
```

One test fails on purpose. Diagnosing and fixing it is Milestone 1.
By the way: the fix may not be where the failing test first points you. :-)

## Where things are

- Source: `src/main/java/edu/cmu/cs214/booking/` (`domain/`, `service/`, `repo/`)
- Tests: `src/test/java/edu/cmu/cs214/booking/`
- Setup: `SETUP.md`
- Your Milestone 2 task: `TASK.md`
- A proposed change you will review in Milestone 3: `changes/agent-attempt.patch` (the handout tells you how to apply it)
- Transcript export script (for Claude Code; modify if using a different tool): `tools/export-transcripts.sh` (the handout tells you when to run it)

See the Lab 1 handout on the course page for the three milestones you show a TA.

## 工具与模型声明

- 工具：Claude Code（命令行）；IDE：VS Code
- 模型：deepseek-flash（Claude Code 配置的会话模型）

## 拓展 3：让智能体审查智能体

把 `changes/agent-attempt.patch` 交给 Claude Code，并按指导书提示它“审查这个 diff 是否违背
`ARCHITECTURE.md` 中的不变式”。它的回答切中要害：`isAvailable` 没有复用 `overlaps`，
而是自己内联重写了一份判断——而且写错了，`[570,630)`、`[540,720)` 这类“已有预订先开始、
延伸进查询区间”的情况会被误判为可用。它还区分了两层：**状态层**没有当场破坏不变式
（`isAvailable` 是只读查询，`book()` 自己仍用 `overlaps` 兜底），但**保护层**已经违背
承诺——javadoc 明确邀请调用者“先查可用性再预订”，一旦有人照做就会真的落下重叠预订。
把问法收窄到“是否违背不变式、是否复用唯一规则”之后，它没有停在“能通过测试”，而是给出了
可验证的具体反例；泛泛地问“这个 diff 有问题吗”，得到的往往是确认偏误。

## 拓展 4：“先查询再预订”的隐患（TOCTOU）

即使 `isAvailable` 完全正确，“先 `isAvailable` 再 `book`”这种用法在并发下仍然危险：
两次调用之间存在时间窗口，另一个用户可能在这段时间里恰好订走了同一时段，于是
“检查时可用”到“使用时已被占用”，这就是 TOCTOU（time-of-check to time-of-use）。
本系统把“检查 + 落库”合并在 `book` 一次调用里，并让它返回 `BookingResult`
（`Confirmed` 或 `Waitlisted`），调用者拿到的就是**那一刻的真实结果**，不需要也不应该
在外面预判可用性。`isAvailable` 只适合做展示层的“提示”，不能当作预订的前置条件。
