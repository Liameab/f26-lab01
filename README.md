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
- 模型：Claude Code 会话中实际使用的模型（按你机器上的会话填写）

## 拓展 3：让智能体审查智能体

把 `changes/agent-attempt.patch` 直接丢给智能体、只问“这个 diff 有问题吗”，它多半回答
“看起来没问题，能通过测试”。换成明确指向不变式的问法——“审查这个 diff 是否违背
`ARCHITECTURE.md` 中的不变式，注意 `overlaps` 是冲突规则的唯一所在”——它才指出
`isAvailable` 自己重写了判断、且只看已有预订的起点，因此 `[570,630)` 这类“先开始、
延伸进查询区间”的预订会被误判为可用。结论：提示词里给出**要对照的规约**，审查才有效；
泛泛地问“有没有问题”，得到的往往是确认偏误。

## 拓展 4：“先查询再预订”的隐患（TOCTOU）

即使 `isAvailable` 完全正确，“先 `isAvailable` 再 `book`”这种用法在并发下仍然危险：
两次调用之间存在时间窗口，另一个用户可能在这段时间里恰好订走了同一时段，于是
“检查时可用”到“使用时已被占用”，这就是 TOCTOU（time-of-check to time-of-use）。
本系统把“检查 + 落库”合并在 `book` 一次调用里，并让它返回 `BookingResult`
（`Confirmed` 或 `Waitlisted`），调用者拿到的就是**那一刻的真实结果**，不需要也不应该
在外面预判可用性。`isAvailable` 只适合做展示层的“提示”，不能当作预订的前置条件。
