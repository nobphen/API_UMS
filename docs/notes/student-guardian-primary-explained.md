# Student Guardian — how the primary ★ works

Rule: **one student = exactly 1 primary guardian** (the school calls this one first).
Each student has their own primary, so the table can have many `true` rows (one per student).

---

## 1. The key of `student_guardians` (composite key)

The table has **no `id` column**. The key is 2 columns together: `student_id` + `guardian_id`.

```java
StudentGuardian.Id id = new StudentGuardian.Id(studentId, request.guardianId());
// studentId = 5 (URL), guardianId = 1 (body)  →  id = (5, 1)
```

- `id` is **one object holding 2 numbers**, not one number.
- `existsById(id)` → `SELECT ... WHERE student_id = 5 AND guardian_id = 1`
- `existsById_StudentId(5)` → `_` = go inside `id` → `WHERE student_id = 5`
- `setId(id)` fills 2 columns (via `@EmbeddedId` + `@Embeddable` + `@Column`), saved at `save(link)`.

| | Create (`link`) | Update |
|---|---|---|
| Object comes from | `toEntity` (empty) | `findLink` (DB row) |
| `setId`? | ✅ yes (id is null) | ❌ no (already loaded; changing it = error) |
| Saving | `save()` → INSERT | automatic at commit → UPDATE |

---

## 2. LINK (create) — the primary flag

```java
boolean primary = request.primary() || !studentGuardianRepository.existsById_StudentId(studentId);

if (primary) {
    studentGuardianRepository.clearPrimaryExcept(studentId, request.guardianId());
}
```

In words: **primary = (admin ticked it) OR (this is the first guardian)**.
Don't forget the `!` — it flips the answer.

| Ticked | Student already has guardians | Calculation | `primary` | UPDATE runs? |
|---|---|---|---|---|
| ❌ | no (first) | `false \|\| !false` = `false \|\| true` | **true** | yes, 0 rows |
| ❌ | yes | `false \|\| !true` = `false \|\| false` | **false** | **no** (skipped) |
| ✅ | yes | `true \|\| ...` | **true** | yes, clears old ★ |

`clearPrimaryExcept(5, 3)`:
```sql
UPDATE student_guardians SET is_primary = false
WHERE student_id = 5 AND guardian_id <> 3
```
- 5 and 3 come from the **request** (URL + body), not from the table.
- WHERE is a search filter: no matching row → 0 rows updated, no error.
- It runs **before** `save` and only when `primary = true`.
- `setPrimary` changes the NEW row; `clearPrimaryExcept` changes the OLD rows.

### Example: student 5

| Request | `primary` | UPDATE | Table after |
|---|---|---|---|
| 1. Father (1), not ticked | true (first) | 0 rows | Father ★ |
| 2. Mother (2), not ticked | false | skipped | Father ★, Mother |
| 3. Aunt (3), **ticked** | true | 1 row (father → false) | Father, Mother, Aunt ★ |

Without `clearPrimaryExcept` in step 3: Father ★ AND Aunt ★ → 2 primaries → the school doesn't know who to call ❌

`is_primary = false` does NOT remove a guardian — all 3 are still guardians. To remove one: unlink (DELETE).

---

## 3. UPDATE — the primary flag

```java
StudentGuardian link = findLink(studentId, guardianId);          // line 1: loaded from DB

if (request.primary()) {                                         // line 2
    studentGuardianRepository.clearPrimaryExcept(studentId, guardianId);
}

studentGuardianMapper.update(request, link);   // line 3: relationship only (primary is ignore)
if (request.primary()) {                                         // line 4
    link.setPrimary(true);
}

Guardian guardian = findGuardian(guardianId);
return toResponse(link, guardian);
```

Key point: **the mapper does not touch `primary`**. When both `if`s are skipped, `primary` keeps
the value `findLink` loaded from the DB.

So in update, `"primary": false` means **"don't change the ★"**, not "remove the ★".
To move the ★: make ANOTHER guardian primary.

### Table before (student 5)

| guardian_id | relationship | is_primary |
|---|---|---|
| 1 | FATHER | false |
| 2 | MOTHER | **true ★** |

### Case A — mother is ★, `PUT /students/5/guardians/2` `{"relationship":"MOTHER","primary":false}`

| Line | What happens | link.primary |
|---|---|---|
| 1 | `findLink(5, 2)` loads the row | **true** (from DB) |
| 2 | `request.primary()` = false → skipped | true |
| 3 | mapper copies relationship only | true |
| 4 | skipped | **true** |
| commit | nothing changed → no UPDATE | |

Table after: the same (mother still ★).

### Case B — father not ★, `PUT /students/5/guardians/1` `{"relationship":"FATHER","primary":true}`

| Line | What happens |
|---|---|
| 1 | `findLink(5, 1)` → primary = false |
| 2 | runs: `UPDATE ... SET is_primary=false WHERE student_id=5 AND guardian_id<>1` → mother → false |
| 3 | mapper → relationship = FATHER |
| 4 | runs: `link.setPrimary(true)` |
| commit | `UPDATE ... SET relationship='FATHER', is_primary=true WHERE student_id=5 AND guardian_id=1` |

Table after:

| guardian_id | relationship | is_primary |
|---|---|---|
| 1 | FATHER | **true ★** |
| 2 | MOTHER | false |

### Summary

| Request | Line 2 (clear others) | Line 4 (set this one) | Result |
|---|---|---|---|
| `"primary": false` | skipped | skipped | primary **unchanged** |
| `"primary": true` | runs | runs | ★ **moves** to this guardian |
