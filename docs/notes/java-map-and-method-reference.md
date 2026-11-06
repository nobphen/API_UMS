# Java notes: `.map()`, `::` and lambdas

Notes for later study. Examples come from the UMS code.

---

## 1. What `facultyMapper::toResponse` means

```
facultyMapper  ::  toResponse
     │          │       │
  the object   "use"  the method
```

- `::` is a **method reference**. It points to a method without calling it yet.
- `facultyMapper` is a field in `FacultyServiceImpl` that Spring injects.
- `toResponse` is declared in `FacultyMapper`:

```java
FacultyResponse toResponse(Faculty faculty);   // 1 Faculty in → 1 FacultyResponse out
```

`FacultyMapper` is an interface. **MapStruct writes the code at build time**, in `build/generated/...`. The generated code looks like this:

```java
public FacultyResponse toResponse(Faculty faculty) {
    if (faculty == null) return null;
    return new FacultyResponse(faculty.getId(), faculty.getCode(), faculty.getName(),
                               faculty.getNameKh(), faculty.getDeanStaffId());
}
```

It copies each field with the same name from the entity to the response.

---

## 2. What `.map()` does

```java
facultyRepository.search(q, pageable)        // Page<Faculty>          (entities from the DB)
        .map(facultyMapper::toResponse);     // Page<FacultyResponse>  (JSON for the API)
```

```
Page<Faculty>                          Page<FacultyResponse>
┌─────────────────────┐   .map()       ┌──────────────────────────┐
│ Faculty(id=1, IT)   │ ──toResponse──▶│ FacultyResponse(1, IT)   │
│ Faculty(id=2, ENG)  │ ──toResponse──▶│ FacultyResponse(2, ENG)  │
│ Faculty(id=3, BUS)  │ ──toResponse──▶│ FacultyResponse(3, BUS)  │
│ total = 3, page 1   │ ───(kept)────▶ │ total = 3, page 1        │
└─────────────────────┘                └──────────────────────────┘
```

`.map()` calls the method **once for each item**. It also keeps the page info: total, page number and size.

---

## 3. Three ways to write the same thing

```java
// A. Method reference: short, used in the project ✅
return page.map(facultyMapper::toResponse);

// B. Lambda: same meaning
return page.map(faculty -> facultyMapper.toResponse(faculty));

// C. Old-style for loop (before Java 8): same meaning, long
Page<Faculty> page = facultyRepository.search(q, pageable);        // 0. get the page from the DB
List<FacultyResponse> list = new ArrayList<>();                     // 1. an empty list
for (Faculty faculty : page.getContent()) {                         // 2. go through each faculty
    list.add(facultyMapper.toResponse(faculty));                    // 3. convert → add to the list
}
return new PageImpl<>(list, pageable, page.getTotalElements());     // 4. wrap the list as a Page again
```

What each part of the old way does:

| Part | Meaning |
|---|---|
| `page.getContent()` | Only the items of the page (`List<Faculty>`) |
| `for (Faculty faculty : ...)` | Takes each item one by one |
| `new PageImpl<>(list, pageable, total)` | Builds a Page again. `Page` is an interface, so you can't write `new Page<>()`; `PageImpl` is Spring's class for it |
| `page.getTotalElements()` | The total number of rows in the DB, not only this page |

`.map()` does steps 1 to 4 for you.

---

## 4. The name in a lambda (`student -> ...`)

```java
.map(student -> studentMapper.toResponse(student, ...))
```

`student` is just a **name for each item**, like the variable in a for loop:

```java
for (Student student : page) { ... }   // same idea
```

You can use any name (`student`, `s`, `abc`), but choose one that is clear.

---

## 5. When `::` works and when it doesn't

`::` works only when the method takes **exactly what `.map()` gives**, which is 1 item:

| `.map()` gives | The method needs | Does `::` work? |
|---|---|---|
| 1 Faculty | `toResponse(Faculty)`: 1 thing | ✅ `facultyMapper::toResponse` |
| 1 Student | `toResponse(Student, List<AddressResponse>)`: 2 things | ❌ use a helper or a lambda |

Student needs the addresses, which come from **another table** (`person_addresses`). A mapper only copies fields and never calls the database, so `StudentServiceImpl` has a helper:

```java
private StudentResponse toResponse(Student student) {          // takes 1 ✅
    return studentMapper.toResponse(student,                    // gives the mapper 2
            personAddressService.getByPerson(student.getPerson().getId()));  // loads the addresses
}
```

Then you use `.map(this::toResponse)`. Here `this` means this class's own method.

---

## 6. The 3 styles in the UMS project

| Situation | Style | Example |
|---|---|---|
| The mapper needs only the entity | `mapper::toResponse` | `FacultyServiceImpl:66`, `RoomServiceImpl:85`, `ProgramServiceImpl:88` |
| Extra data from another table is needed | `this::toResponse` (a helper) | `StudentServiceImpl` list / getByUser, `RoleServiceImpl:48` |
| No mapper, a short one-off | lambda `x -> ...` | `UserSessionServiceImpl:191`, `AuthServiceImpl:224` |

---

## 7. Idea for later: a light list without addresses

`this::toResponse` in the student list runs **1 extra query per student**: a page of 20 means 1 + 20 = 21 queries. If the list gets slow, add this to `StudentMapper`:

```java
/** For the list page: same as toResponse but without addresses (saves 1 query per student). */
@Mapping(target = "addresses", ignore = true)
StudentResponse toListResponse(Student student);
```

Then use `.map(studentMapper::toListResponse)`, which is 1 query for the whole page. The detail page (`GET /students/{id}`) still shows the addresses.

---

## Things to search later

- Java 8 **method reference** (`::`)
- Java **lambda expression** (`x -> ...`)
- Java **Stream API**: `map`, `filter`, `collect`
- Spring Data **`Page.map()`** and **`PageImpl`**
- **MapStruct**: how it generates code, `@Mapping`, `@MappingTarget`
- The **N+1 query problem** (why 21 queries is slow)
