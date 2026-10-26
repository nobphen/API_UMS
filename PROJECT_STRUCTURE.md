# University Management System — Full Project Structure

Spring Boot 4.1 · Java 21 · PostgreSQL · package by feature · 20 modules · 120 models (one per table)

How to read it:
- Each module (and each sub-feature inside a large module) has the same four folders: `api/` (controllers + DTOs), `application/` (service interfaces, `impl/` with the `ServiceImpl` classes, and mappers), `domain/` (JPA models), `infrastructure/` (repositories).
- Every table has a model in `domain/` and a repository in `infrastructure/` — 120 models for 122 tables, because `role_permissions` is a @ManyToMany inside `Role` and `document_sequences` is used by `DocumentNumberService` with plain SQL, so neither has a class of its own.
- Main tables also get a controller, request/response DTOs, a service and a mapper. Child tables (invoice_items, user_roles, route_stops…) are saved through their parent's service, so they have no controller of their own.
- Every service is an interface (`StudentService`) with its implementation in `application/impl/` (`StudentServiceImpl`, annotated `@Service @Transactional`). Controllers depend on the interface only.
- Each module has a `<Module>Permissions.java` (e.g. `PeoplePermissions`) whose constants copy the `code` column of `V2__seed_permissions.sql` for that module. Controllers protect endpoints with `@RequirePermission(PeoplePermissions.STUDENT_CREATE)` (in `common/security/`); which roles hold each code lives in `role_permissions` and is changed with the roles API.
- Pluggable parts have several implementations behind one interface: file storage (MinIO / S3), payment gateways (ABA / KHQR / Wing), notification senders (FCM / email / Telegram).
- `V1__init_schema.sql` is `ums_schema.sql`, and `V2__seed_permissions.sql` is `UMS_V2__seed_permissions.sql` (copies in the project root, updated automatically on every build by the Gradle task `syncSchemaCopies`).
- `keys/` (the RSA key pair) is git-ignored and not listed; see the README to generate it.

```
ums/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── ums/
│   │   │           ├── academic/
│   │   │           │   ├── calendar/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── AcademicYearRequest.java
│   │   │           │   │   │   │   ├── AcademicYearResponse.java
│   │   │           │   │   │   │   ├── SemesterRequest.java
│   │   │           │   │   │   │   └── SemesterResponse.java
│   │   │           │   │   │   ├── AcademicYearController.java
│   │   │           │   │   │   └── SemesterController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── AcademicYearServiceImpl.java
│   │   │           │   │   │   │   └── SemesterServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   ├── AcademicYearMapper.java
│   │   │           │   │   │   │   └── SemesterMapper.java
│   │   │           │   │   │   ├── AcademicYearService.java
│   │   │           │   │   │   └── SemesterService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── AcademicYear.java
│   │   │           │   │   │   └── Semester.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── AcademicYearRepository.java
│   │   │           │   │       └── SemesterRepository.java
│   │   │           │   ├── course/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── CourseRequest.java
│   │   │           │   │   │   │   ├── CourseResponse.java
│   │   │           │   │   │   │   ├── CurriculumRequest.java
│   │   │           │   │   │   │   └── CurriculumResponse.java
│   │   │           │   │   │   ├── CourseController.java
│   │   │           │   │   │   └── CurriculumController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── CourseServiceImpl.java
│   │   │           │   │   │   │   └── CurriculumServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   ├── CourseMapper.java
│   │   │           │   │   │   │   └── CurriculumMapper.java
│   │   │           │   │   │   ├── CourseService.java
│   │   │           │   │   │   └── CurriculumService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── Course.java
│   │   │           │   │   │   ├── CoursePrerequisite.java
│   │   │           │   │   │   ├── Curriculum.java
│   │   │           │   │   │   └── CurriculumCourse.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── CoursePrerequisiteRepository.java
│   │   │           │   │       ├── CourseRepository.java
│   │   │           │   │       ├── CurriculumCourseRepository.java
│   │   │           │   │       └── CurriculumRepository.java
│   │   │           │   ├── section/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── ClassSectionRequest.java
│   │   │           │   │   │   │   └── ClassSectionResponse.java
│   │   │           │   │   │   └── ClassSectionController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── ClassSectionServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── ClassSectionMapper.java
│   │   │           │   │   │   └── ClassSectionService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── ClassSchedule.java
│   │   │           │   │   │   ├── ClassSection.java
│   │   │           │   │   │   └── ClassSectionLecturer.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── ClassScheduleRepository.java
│   │   │           │   │       ├── ClassSectionLecturerRepository.java
│   │   │           │   │       └── ClassSectionRepository.java
│   │   │           │   └── AcademicPermissions.java
│   │   │           ├── admission/
│   │   │           │   ├── application/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── ApplicantRequest.java
│   │   │           │   │   │   │   ├── ApplicantResponse.java
│   │   │           │   │   │   │   ├── ApplicationRequest.java
│   │   │           │   │   │   │   └── ApplicationResponse.java
│   │   │           │   │   │   ├── ApplicantController.java
│   │   │           │   │   │   └── ApplicationController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── ApplicantServiceImpl.java
│   │   │           │   │   │   │   └── ApplicationServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   ├── ApplicantMapper.java
│   │   │           │   │   │   │   └── ApplicationMapper.java
│   │   │           │   │   │   ├── ApplicantService.java
│   │   │           │   │   │   └── ApplicationService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── Applicant.java
│   │   │           │   │   │   ├── Application.java
│   │   │           │   │   │   ├── ApplicationDocument.java
│   │   │           │   │   │   └── ApplicationResult.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── ApplicantRepository.java
│   │   │           │   │       ├── ApplicationDocumentRepository.java
│   │   │           │   │       ├── ApplicationRepository.java
│   │   │           │   │       └── ApplicationResultRepository.java
│   │   │           │   ├── formsale/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── ApplicationFormSaleRequest.java
│   │   │           │   │   │   │   └── ApplicationFormSaleResponse.java
│   │   │           │   │   │   └── ApplicationFormSaleController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── ApplicationFormSaleServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── ApplicationFormSaleMapper.java
│   │   │           │   │   │   └── ApplicationFormSaleService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   └── ApplicationFormSale.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       └── ApplicationFormSaleRepository.java
│   │   │           │   ├── intake/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── AdmissionIntakeRequest.java
│   │   │           │   │   │   │   └── AdmissionIntakeResponse.java
│   │   │           │   │   │   └── AdmissionIntakeController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── AdmissionIntakeServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── AdmissionIntakeMapper.java
│   │   │           │   │   │   └── AdmissionIntakeService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── AdmissionIntake.java
│   │   │           │   │   │   └── IntakeProgram.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── AdmissionIntakeRepository.java
│   │   │           │   │       └── IntakeProgramRepository.java
│   │   │           │   └── AdmissionPermissions.java
│   │   │           ├── asset/
│   │   │           │   ├── api/
│   │   │           │   │   ├── dto/
│   │   │           │   │   │   ├── AssetCategoryRequest.java
│   │   │           │   │   │   ├── AssetCategoryResponse.java
│   │   │           │   │   │   ├── AssetRequest.java
│   │   │           │   │   │   └── AssetResponse.java
│   │   │           │   │   ├── AssetCategoryController.java
│   │   │           │   │   └── AssetController.java
│   │   │           │   ├── application/
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   ├── AssetCategoryServiceImpl.java
│   │   │           │   │   │   └── AssetServiceImpl.java
│   │   │           │   │   ├── mapper/
│   │   │           │   │   │   ├── AssetCategoryMapper.java
│   │   │           │   │   │   └── AssetMapper.java
│   │   │           │   │   ├── AssetCategoryService.java
│   │   │           │   │   └── AssetService.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── Asset.java
│   │   │           │   │   ├── AssetAssignment.java
│   │   │           │   │   ├── AssetCategory.java
│   │   │           │   │   └── AssetMaintenance.java
│   │   │           │   ├── infrastructure/
│   │   │           │   │   ├── AssetAssignmentRepository.java
│   │   │           │   │   ├── AssetCategoryRepository.java
│   │   │           │   │   ├── AssetMaintenanceRepository.java
│   │   │           │   │   └── AssetRepository.java
│   │   │           │   └── AssetPermissions.java
│   │   │           ├── chat/
│   │   │           │   ├── conversation/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── ConversationRequest.java
│   │   │           │   │   │   │   ├── ConversationResponse.java
│   │   │           │   │   │   │   ├── MessageRequest.java
│   │   │           │   │   │   │   └── MessageResponse.java
│   │   │           │   │   │   ├── ConversationController.java
│   │   │           │   │   │   └── MessageController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── ConversationServiceImpl.java
│   │   │           │   │   │   │   └── MessageServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   ├── ConversationMapper.java
│   │   │           │   │   │   │   └── MessageMapper.java
│   │   │           │   │   │   ├── ConversationService.java
│   │   │           │   │   │   └── MessageService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── Conversation.java
│   │   │           │   │   │   ├── ConversationParticipant.java
│   │   │           │   │   │   ├── Message.java
│   │   │           │   │   │   └── MessageAttachment.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── ConversationParticipantRepository.java
│   │   │           │   │       ├── ConversationRepository.java
│   │   │           │   │       ├── MessageAttachmentRepository.java
│   │   │           │   │       └── MessageRepository.java
│   │   │           │   ├── notification/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── NotificationRequest.java
│   │   │           │   │   │   │   └── NotificationResponse.java
│   │   │           │   │   │   └── NotificationController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── NotificationServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── NotificationMapper.java
│   │   │           │   │   │   ├── sender/
│   │   │           │   │   │   │   ├── impl/
│   │   │           │   │   │   │   │   ├── EmailNotificationSender.java
│   │   │           │   │   │   │   │   ├── FcmNotificationSender.java
│   │   │           │   │   │   │   │   └── TelegramNotificationSender.java
│   │   │           │   │   │   │   └── NotificationSender.java
│   │   │           │   │   │   └── NotificationService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   └── Notification.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       └── NotificationRepository.java
│   │   │           │   └── ChatPermissions.java
│   │   │           ├── common/
│   │   │           │   ├── audit/
│   │   │           │   │   ├── AuditLogListener.java
│   │   │           │   │   └── AuditorAwareImpl.java
│   │   │           │   ├── entity/
│   │   │           │   │   ├── BaseEntity.java
│   │   │           │   │   └── SoftDeletableEntity.java
│   │   │           │   ├── exception/
│   │   │           │   │   ├── BusinessException.java
│   │   │           │   │   ├── GlobalExceptionHandler.java
│   │   │           │   │   └── NotFoundException.java
│   │   │           │   ├── numbering/
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   └── DocumentNumberServiceImpl.java
│   │   │           │   │   └── DocumentNumberService.java
│   │   │           │   ├── response/
│   │   │           │   │   ├── ApiResponse.java
│   │   │           │   │   └── PageResponse.java
│   │   │           │   ├── security/
│   │   │           │   │   └── RequirePermission.java
│   │   │           │   ├── storage/
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   ├── MinioFileStorageService.java
│   │   │           │   │   │   └── S3FileStorageService.java
│   │   │           │   │   └── FileStorageService.java
│   │   │           │   ├── util/
│   │   │           │   │   ├── DateUtils.java
│   │   │           │   │   └── PhoneNumbers.java
│   │   │           │   └── package-info.java
│   │   │           ├── config/
│   │   │           │   ├── CorsConfig.java
│   │   │           │   ├── JpaConfig.java
│   │   │           │   ├── JwtConfig.java
│   │   │           │   ├── LocaleConfig.java
│   │   │           │   ├── OpenApiConfig.java
│   │   │           │   ├── PasswordEncoderConfig.java
│   │   │           │   ├── RedisConfig.java
│   │   │           │   ├── SecurityConfig.java
│   │   │           │   └── WebSocketConfig.java
│   │   │           ├── engagement/
│   │   │           │   ├── alumni/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── AlumniProfileRequest.java
│   │   │           │   │   │   │   └── AlumniProfileResponse.java
│   │   │           │   │   │   └── AlumniProfileController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── AlumniProfileServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── AlumniProfileMapper.java
│   │   │           │   │   │   └── AlumniProfileService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   └── AlumniProfile.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       └── AlumniProfileRepository.java
│   │   │           │   ├── event/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── EventRequest.java
│   │   │           │   │   │   │   └── EventResponse.java
│   │   │           │   │   │   └── EventController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── EventServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── EventMapper.java
│   │   │           │   │   │   └── EventService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── Event.java
│   │   │           │   │   │   └── EventRegistration.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── EventRegistrationRepository.java
│   │   │           │   │       └── EventRepository.java
│   │   │           │   ├── survey/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── SurveyRequest.java
│   │   │           │   │   │   │   └── SurveyResponse.java
│   │   │           │   │   │   └── SurveyController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── SurveyServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── SurveyMapper.java
│   │   │           │   │   │   └── SurveyService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── Survey.java
│   │   │           │   │   │   ├── SurveyQuestion.java
│   │   │           │   │   │   └── SurveyResponse.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── SurveyQuestionRepository.java
│   │   │           │   │       ├── SurveyRepository.java
│   │   │           │   │       └── SurveyResponseRepository.java
│   │   │           │   └── EngagementPermissions.java
│   │   │           ├── enrollment/
│   │   │           │   ├── attendance/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── AttendanceSessionRequest.java
│   │   │           │   │   │   │   └── AttendanceSessionResponse.java
│   │   │           │   │   │   └── AttendanceSessionController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── AttendanceSessionServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── AttendanceSessionMapper.java
│   │   │           │   │   │   └── AttendanceSessionService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── AttendanceRecord.java
│   │   │           │   │   │   └── AttendanceSession.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── AttendanceRecordRepository.java
│   │   │           │   │       └── AttendanceSessionRepository.java
│   │   │           │   ├── grading/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── AssessmentRequest.java
│   │   │           │   │   │   │   ├── AssessmentResponse.java
│   │   │           │   │   │   │   ├── AssessmentTypeRequest.java
│   │   │           │   │   │   │   ├── AssessmentTypeResponse.java
│   │   │           │   │   │   │   ├── FinalGradeRequest.java
│   │   │           │   │   │   │   ├── FinalGradeResponse.java
│   │   │           │   │   │   │   ├── GradeScaleRequest.java
│   │   │           │   │   │   │   ├── GradeScaleResponse.java
│   │   │           │   │   │   │   ├── StudentSemesterResultRequest.java
│   │   │           │   │   │   │   └── StudentSemesterResultResponse.java
│   │   │           │   │   │   ├── AssessmentController.java
│   │   │           │   │   │   ├── AssessmentTypeController.java
│   │   │           │   │   │   ├── FinalGradeController.java
│   │   │           │   │   │   ├── GradeScaleController.java
│   │   │           │   │   │   └── StudentSemesterResultController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── AssessmentServiceImpl.java
│   │   │           │   │   │   │   ├── AssessmentTypeServiceImpl.java
│   │   │           │   │   │   │   ├── FinalGradeServiceImpl.java
│   │   │           │   │   │   │   ├── GradeScaleServiceImpl.java
│   │   │           │   │   │   │   └── StudentSemesterResultServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   ├── AssessmentMapper.java
│   │   │           │   │   │   │   ├── AssessmentTypeMapper.java
│   │   │           │   │   │   │   ├── FinalGradeMapper.java
│   │   │           │   │   │   │   ├── GradeScaleMapper.java
│   │   │           │   │   │   │   └── StudentSemesterResultMapper.java
│   │   │           │   │   │   ├── AssessmentService.java
│   │   │           │   │   │   ├── AssessmentTypeService.java
│   │   │           │   │   │   ├── FinalGradeService.java
│   │   │           │   │   │   ├── GradeScaleService.java
│   │   │           │   │   │   └── StudentSemesterResultService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── Assessment.java
│   │   │           │   │   │   ├── AssessmentScore.java
│   │   │           │   │   │   ├── AssessmentType.java
│   │   │           │   │   │   ├── FinalGrade.java
│   │   │           │   │   │   ├── GradeScale.java
│   │   │           │   │   │   └── StudentSemesterResult.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── AssessmentRepository.java
│   │   │           │   │       ├── AssessmentScoreRepository.java
│   │   │           │   │       ├── AssessmentTypeRepository.java
│   │   │           │   │       ├── FinalGradeRepository.java
│   │   │           │   │       ├── GradeScaleRepository.java
│   │   │           │   │       └── StudentSemesterResultRepository.java
│   │   │           │   ├── registration/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── EnrollmentRequest.java
│   │   │           │   │   │   │   └── EnrollmentResponse.java
│   │   │           │   │   │   └── EnrollmentController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── EnrollmentServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── EnrollmentMapper.java
│   │   │           │   │   │   └── EnrollmentService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   └── Enrollment.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       └── EnrollmentRepository.java
│   │   │           │   └── EnrollmentPermissions.java
│   │   │           ├── exam/
│   │   │           │   ├── api/
│   │   │           │   │   ├── dto/
│   │   │           │   │   │   ├── ExamScheduleRequest.java
│   │   │           │   │   │   ├── ExamScheduleResponse.java
│   │   │           │   │   │   ├── GradeAppealRequest.java
│   │   │           │   │   │   └── GradeAppealResponse.java
│   │   │           │   │   ├── ExamScheduleController.java
│   │   │           │   │   └── GradeAppealController.java
│   │   │           │   ├── application/
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   ├── ExamScheduleServiceImpl.java
│   │   │           │   │   │   └── GradeAppealServiceImpl.java
│   │   │           │   │   ├── mapper/
│   │   │           │   │   │   ├── ExamScheduleMapper.java
│   │   │           │   │   │   └── GradeAppealMapper.java
│   │   │           │   │   ├── ExamScheduleService.java
│   │   │           │   │   └── GradeAppealService.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── ExamInvigilator.java
│   │   │           │   │   ├── ExamSchedule.java
│   │   │           │   │   ├── ExamSeatAssignment.java
│   │   │           │   │   └── GradeAppeal.java
│   │   │           │   ├── infrastructure/
│   │   │           │   │   ├── ExamInvigilatorRepository.java
│   │   │           │   │   ├── ExamScheduleRepository.java
│   │   │           │   │   ├── ExamSeatAssignmentRepository.java
│   │   │           │   │   └── GradeAppealRepository.java
│   │   │           │   └── ExamPermissions.java
│   │   │           ├── finance/
│   │   │           │   ├── fee/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── FeeStructureRequest.java
│   │   │           │   │   │   │   ├── FeeStructureResponse.java
│   │   │           │   │   │   │   ├── FeeTypeRequest.java
│   │   │           │   │   │   │   └── FeeTypeResponse.java
│   │   │           │   │   │   ├── FeeStructureController.java
│   │   │           │   │   │   └── FeeTypeController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── FeeStructureServiceImpl.java
│   │   │           │   │   │   │   └── FeeTypeServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   ├── FeeStructureMapper.java
│   │   │           │   │   │   │   └── FeeTypeMapper.java
│   │   │           │   │   │   ├── FeeStructureService.java
│   │   │           │   │   │   └── FeeTypeService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── FeeStructure.java
│   │   │           │   │   │   └── FeeType.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── FeeStructureRepository.java
│   │   │           │   │       └── FeeTypeRepository.java
│   │   │           │   ├── invoice/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── InvoiceRequest.java
│   │   │           │   │   │   │   └── InvoiceResponse.java
│   │   │           │   │   │   └── InvoiceController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── InvoiceServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── InvoiceMapper.java
│   │   │           │   │   │   └── InvoiceService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── Invoice.java
│   │   │           │   │   │   └── InvoiceItem.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── InvoiceItemRepository.java
│   │   │           │   │       └── InvoiceRepository.java
│   │   │           │   ├── payment/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── PaymentRequest.java
│   │   │           │   │   │   │   └── PaymentResponse.java
│   │   │           │   │   │   └── PaymentController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── gateway/
│   │   │           │   │   │   │   ├── impl/
│   │   │           │   │   │   │   │   ├── AbaPaymentGateway.java
│   │   │           │   │   │   │   │   ├── KhqrPaymentGateway.java
│   │   │           │   │   │   │   │   └── WingPaymentGateway.java
│   │   │           │   │   │   │   └── PaymentGateway.java
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── PaymentServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── PaymentMapper.java
│   │   │           │   │   │   └── PaymentService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   └── Payment.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       └── PaymentRepository.java
│   │   │           │   ├── scholarship/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── ScholarshipRequest.java
│   │   │           │   │   │   │   ├── ScholarshipResponse.java
│   │   │           │   │   │   │   ├── StudentScholarshipRequest.java
│   │   │           │   │   │   │   └── StudentScholarshipResponse.java
│   │   │           │   │   │   ├── ScholarshipController.java
│   │   │           │   │   │   └── StudentScholarshipController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── ScholarshipServiceImpl.java
│   │   │           │   │   │   │   └── StudentScholarshipServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   ├── ScholarshipMapper.java
│   │   │           │   │   │   │   └── StudentScholarshipMapper.java
│   │   │           │   │   │   ├── ScholarshipService.java
│   │   │           │   │   │   └── StudentScholarshipService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── Scholarship.java
│   │   │           │   │   │   └── StudentScholarship.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── ScholarshipRepository.java
│   │   │           │   │       └── StudentScholarshipRepository.java
│   │   │           │   ├── Currency.java
│   │   │           │   └── FinancePermissions.java
│   │   │           ├── graduation/
│   │   │           │   ├── api/
│   │   │           │   │   ├── dto/
│   │   │           │   │   │   ├── CertificateRequest.java
│   │   │           │   │   │   ├── CertificateResponse.java
│   │   │           │   │   │   ├── GraduationApplicationRequest.java
│   │   │           │   │   │   ├── GraduationApplicationResponse.java
│   │   │           │   │   │   ├── ThesisRequest.java
│   │   │           │   │   │   ├── ThesisResponse.java
│   │   │           │   │   │   ├── TranscriptRequestRequest.java
│   │   │           │   │   │   └── TranscriptRequestResponse.java
│   │   │           │   │   ├── CertificateController.java
│   │   │           │   │   ├── GraduationApplicationController.java
│   │   │           │   │   ├── ThesisController.java
│   │   │           │   │   └── TranscriptRequestController.java
│   │   │           │   ├── application/
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   ├── CertificateServiceImpl.java
│   │   │           │   │   │   ├── GraduationApplicationServiceImpl.java
│   │   │           │   │   │   ├── ThesisServiceImpl.java
│   │   │           │   │   │   └── TranscriptRequestServiceImpl.java
│   │   │           │   │   ├── mapper/
│   │   │           │   │   │   ├── CertificateMapper.java
│   │   │           │   │   │   ├── GraduationApplicationMapper.java
│   │   │           │   │   │   ├── ThesisMapper.java
│   │   │           │   │   │   └── TranscriptRequestMapper.java
│   │   │           │   │   ├── CertificateService.java
│   │   │           │   │   ├── GraduationApplicationService.java
│   │   │           │   │   ├── ThesisService.java
│   │   │           │   │   └── TranscriptRequestService.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── Certificate.java
│   │   │           │   │   ├── GraduationApplication.java
│   │   │           │   │   ├── Thesis.java
│   │   │           │   │   ├── ThesisCommitteeMember.java
│   │   │           │   │   ├── ThesisSupervisor.java
│   │   │           │   │   └── TranscriptRequest.java
│   │   │           │   ├── infrastructure/
│   │   │           │   │   ├── CertificateRepository.java
│   │   │           │   │   ├── GraduationApplicationRepository.java
│   │   │           │   │   ├── ThesisCommitteeMemberRepository.java
│   │   │           │   │   ├── ThesisRepository.java
│   │   │           │   │   ├── ThesisSupervisorRepository.java
│   │   │           │   │   └── TranscriptRequestRepository.java
│   │   │           │   └── GraduationPermissions.java
│   │   │           ├── hostel/
│   │   │           │   ├── api/
│   │   │           │   │   ├── dto/
│   │   │           │   │   │   ├── HostelAllocationRequest.java
│   │   │           │   │   │   ├── HostelAllocationResponse.java
│   │   │           │   │   │   ├── HostelMaintenanceRequestRequest.java
│   │   │           │   │   │   ├── HostelMaintenanceRequestResponse.java
│   │   │           │   │   │   ├── HostelRequest.java
│   │   │           │   │   │   ├── HostelResponse.java
│   │   │           │   │   │   ├── HostelRoomRequest.java
│   │   │           │   │   │   └── HostelRoomResponse.java
│   │   │           │   │   ├── HostelAllocationController.java
│   │   │           │   │   ├── HostelController.java
│   │   │           │   │   ├── HostelMaintenanceRequestController.java
│   │   │           │   │   └── HostelRoomController.java
│   │   │           │   ├── application/
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   ├── HostelAllocationServiceImpl.java
│   │   │           │   │   │   ├── HostelMaintenanceRequestServiceImpl.java
│   │   │           │   │   │   ├── HostelRoomServiceImpl.java
│   │   │           │   │   │   └── HostelServiceImpl.java
│   │   │           │   │   ├── mapper/
│   │   │           │   │   │   ├── HostelAllocationMapper.java
│   │   │           │   │   │   ├── HostelMaintenanceRequestMapper.java
│   │   │           │   │   │   ├── HostelMapper.java
│   │   │           │   │   │   └── HostelRoomMapper.java
│   │   │           │   │   ├── HostelAllocationService.java
│   │   │           │   │   ├── HostelMaintenanceRequestService.java
│   │   │           │   │   ├── HostelRoomService.java
│   │   │           │   │   └── HostelService.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── Hostel.java
│   │   │           │   │   ├── HostelAllocation.java
│   │   │           │   │   ├── HostelBed.java
│   │   │           │   │   ├── HostelMaintenanceRequest.java
│   │   │           │   │   └── HostelRoom.java
│   │   │           │   ├── infrastructure/
│   │   │           │   │   ├── HostelAllocationRepository.java
│   │   │           │   │   ├── HostelBedRepository.java
│   │   │           │   │   ├── HostelMaintenanceRequestRepository.java
│   │   │           │   │   ├── HostelRepository.java
│   │   │           │   │   └── HostelRoomRepository.java
│   │   │           │   └── HostelPermissions.java
│   │   │           ├── hr/
│   │   │           │   ├── contract/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── EmploymentContractRequest.java
│   │   │           │   │   │   │   ├── EmploymentContractResponse.java
│   │   │           │   │   │   │   ├── SalaryComponentRequest.java
│   │   │           │   │   │   │   └── SalaryComponentResponse.java
│   │   │           │   │   │   ├── EmploymentContractController.java
│   │   │           │   │   │   └── SalaryComponentController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── EmploymentContractServiceImpl.java
│   │   │           │   │   │   │   └── SalaryComponentServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   ├── EmploymentContractMapper.java
│   │   │           │   │   │   │   └── SalaryComponentMapper.java
│   │   │           │   │   │   ├── EmploymentContractService.java
│   │   │           │   │   │   └── SalaryComponentService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── EmploymentContract.java
│   │   │           │   │   │   ├── SalaryComponent.java
│   │   │           │   │   │   └── StaffSalaryComponent.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── EmploymentContractRepository.java
│   │   │           │   │       ├── SalaryComponentRepository.java
│   │   │           │   │       └── StaffSalaryComponentRepository.java
│   │   │           │   ├── leave/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── StaffLeaveRequestRequest.java
│   │   │           │   │   │   │   ├── StaffLeaveRequestResponse.java
│   │   │           │   │   │   │   ├── StaffLeaveTypeRequest.java
│   │   │           │   │   │   │   └── StaffLeaveTypeResponse.java
│   │   │           │   │   │   ├── StaffLeaveRequestController.java
│   │   │           │   │   │   └── StaffLeaveTypeController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── StaffLeaveRequestServiceImpl.java
│   │   │           │   │   │   │   └── StaffLeaveTypeServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   ├── StaffLeaveRequestMapper.java
│   │   │           │   │   │   │   └── StaffLeaveTypeMapper.java
│   │   │           │   │   │   ├── StaffLeaveRequestService.java
│   │   │           │   │   │   └── StaffLeaveTypeService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── StaffLeaveRequest.java
│   │   │           │   │   │   └── StaffLeaveType.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── StaffLeaveRequestRepository.java
│   │   │           │   │       └── StaffLeaveTypeRepository.java
│   │   │           │   ├── payroll/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── PayrollPeriodRequest.java
│   │   │           │   │   │   │   ├── PayrollPeriodResponse.java
│   │   │           │   │   │   │   ├── PayslipRequest.java
│   │   │           │   │   │   │   ├── PayslipResponse.java
│   │   │           │   │   │   │   ├── TeachingLoadRequest.java
│   │   │           │   │   │   │   └── TeachingLoadResponse.java
│   │   │           │   │   │   ├── PayrollPeriodController.java
│   │   │           │   │   │   ├── PayslipController.java
│   │   │           │   │   │   └── TeachingLoadController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── PayrollPeriodServiceImpl.java
│   │   │           │   │   │   │   ├── PayslipServiceImpl.java
│   │   │           │   │   │   │   └── TeachingLoadServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   ├── PayrollPeriodMapper.java
│   │   │           │   │   │   │   ├── PayslipMapper.java
│   │   │           │   │   │   │   └── TeachingLoadMapper.java
│   │   │           │   │   │   ├── PayrollPeriodService.java
│   │   │           │   │   │   ├── PayslipService.java
│   │   │           │   │   │   └── TeachingLoadService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── PayrollPeriod.java
│   │   │           │   │   │   ├── Payslip.java
│   │   │           │   │   │   ├── PayslipItem.java
│   │   │           │   │   │   └── TeachingLoad.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── PayrollPeriodRepository.java
│   │   │           │   │       ├── PayslipItemRepository.java
│   │   │           │   │       ├── PayslipRepository.java
│   │   │           │   │       └── TeachingLoadRepository.java
│   │   │           │   └── HrPermissions.java
│   │   │           ├── library/
│   │   │           │   ├── api/
│   │   │           │   │   ├── dto/
│   │   │           │   │   │   ├── BookCopyRequest.java
│   │   │           │   │   │   ├── BookCopyResponse.java
│   │   │           │   │   │   ├── BookLoanRequest.java
│   │   │           │   │   │   ├── BookLoanResponse.java
│   │   │           │   │   │   ├── BookRequest.java
│   │   │           │   │   │   ├── BookReservationRequest.java
│   │   │           │   │   │   ├── BookReservationResponse.java
│   │   │           │   │   │   ├── BookResponse.java
│   │   │           │   │   │   ├── LibraryCategoryRequest.java
│   │   │           │   │   │   ├── LibraryCategoryResponse.java
│   │   │           │   │   │   ├── LibraryFineRequest.java
│   │   │           │   │   │   └── LibraryFineResponse.java
│   │   │           │   │   ├── BookController.java
│   │   │           │   │   ├── BookCopyController.java
│   │   │           │   │   ├── BookLoanController.java
│   │   │           │   │   ├── BookReservationController.java
│   │   │           │   │   ├── LibraryCategoryController.java
│   │   │           │   │   └── LibraryFineController.java
│   │   │           │   ├── application/
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   ├── BookCopyServiceImpl.java
│   │   │           │   │   │   ├── BookLoanServiceImpl.java
│   │   │           │   │   │   ├── BookReservationServiceImpl.java
│   │   │           │   │   │   ├── BookServiceImpl.java
│   │   │           │   │   │   ├── LibraryCategoryServiceImpl.java
│   │   │           │   │   │   └── LibraryFineServiceImpl.java
│   │   │           │   │   ├── mapper/
│   │   │           │   │   │   ├── BookCopyMapper.java
│   │   │           │   │   │   ├── BookLoanMapper.java
│   │   │           │   │   │   ├── BookMapper.java
│   │   │           │   │   │   ├── BookReservationMapper.java
│   │   │           │   │   │   ├── LibraryCategoryMapper.java
│   │   │           │   │   │   └── LibraryFineMapper.java
│   │   │           │   │   ├── BookCopyService.java
│   │   │           │   │   ├── BookLoanService.java
│   │   │           │   │   ├── BookReservationService.java
│   │   │           │   │   ├── BookService.java
│   │   │           │   │   ├── LibraryCategoryService.java
│   │   │           │   │   └── LibraryFineService.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── Book.java
│   │   │           │   │   ├── BookCopy.java
│   │   │           │   │   ├── BookLoan.java
│   │   │           │   │   ├── BookReservation.java
│   │   │           │   │   ├── LibraryCategory.java
│   │   │           │   │   ├── LibraryFine.java
│   │   │           │   │   └── LibraryLoanPolicy.java
│   │   │           │   ├── infrastructure/
│   │   │           │   │   ├── BookCopyRepository.java
│   │   │           │   │   ├── BookLoanRepository.java
│   │   │           │   │   ├── BookRepository.java
│   │   │           │   │   ├── BookReservationRepository.java
│   │   │           │   │   ├── LibraryCategoryRepository.java
│   │   │           │   │   ├── LibraryFineRepository.java
│   │   │           │   │   └── LibraryLoanPolicyRepository.java
│   │   │           │   └── LibraryPermissions.java
│   │   │           ├── lms/
│   │   │           │   ├── api/
│   │   │           │   │   ├── dto/
│   │   │           │   │   │   ├── AssignmentSubmissionRequest.java
│   │   │           │   │   │   ├── AssignmentSubmissionResponse.java
│   │   │           │   │   │   ├── CourseMaterialRequest.java
│   │   │           │   │   │   ├── CourseMaterialResponse.java
│   │   │           │   │   │   ├── QuizAttemptRequest.java
│   │   │           │   │   │   ├── QuizAttemptResponse.java
│   │   │           │   │   │   ├── QuizQuestionRequest.java
│   │   │           │   │   │   └── QuizQuestionResponse.java
│   │   │           │   │   ├── AssignmentSubmissionController.java
│   │   │           │   │   ├── CourseMaterialController.java
│   │   │           │   │   ├── QuizAttemptController.java
│   │   │           │   │   └── QuizQuestionController.java
│   │   │           │   ├── application/
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   ├── AssignmentSubmissionServiceImpl.java
│   │   │           │   │   │   ├── CourseMaterialServiceImpl.java
│   │   │           │   │   │   ├── QuizAttemptServiceImpl.java
│   │   │           │   │   │   └── QuizQuestionServiceImpl.java
│   │   │           │   │   ├── mapper/
│   │   │           │   │   │   ├── AssignmentSubmissionMapper.java
│   │   │           │   │   │   ├── CourseMaterialMapper.java
│   │   │           │   │   │   ├── QuizAttemptMapper.java
│   │   │           │   │   │   └── QuizQuestionMapper.java
│   │   │           │   │   ├── AssignmentSubmissionService.java
│   │   │           │   │   ├── CourseMaterialService.java
│   │   │           │   │   ├── QuizAttemptService.java
│   │   │           │   │   └── QuizQuestionService.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── AssignmentSubmission.java
│   │   │           │   │   ├── CourseMaterial.java
│   │   │           │   │   ├── QuizAttempt.java
│   │   │           │   │   ├── QuizQuestion.java
│   │   │           │   │   └── QuizQuestionOption.java
│   │   │           │   ├── infrastructure/
│   │   │           │   │   ├── AssignmentSubmissionRepository.java
│   │   │           │   │   ├── CourseMaterialRepository.java
│   │   │           │   │   ├── QuizAttemptRepository.java
│   │   │           │   │   ├── QuizQuestionOptionRepository.java
│   │   │           │   │   └── QuizQuestionRepository.java
│   │   │           │   └── LmsPermissions.java
│   │   │           ├── organization/
│   │   │           │   ├── api/
│   │   │           │   │   ├── dto/
│   │   │           │   │   │   ├── BuildingRequest.java
│   │   │           │   │   │   ├── BuildingResponse.java
│   │   │           │   │   │   ├── DepartmentRequest.java
│   │   │           │   │   │   ├── DepartmentResponse.java
│   │   │           │   │   │   ├── FacultyRequest.java
│   │   │           │   │   │   ├── FacultyResponse.java
│   │   │           │   │   │   ├── ProgramRequest.java
│   │   │           │   │   │   ├── ProgramResponse.java
│   │   │           │   │   │   ├── RoomRequest.java
│   │   │           │   │   │   └── RoomResponse.java
│   │   │           │   │   ├── BuildingController.java
│   │   │           │   │   ├── DepartmentController.java
│   │   │           │   │   ├── FacultyController.java
│   │   │           │   │   ├── ProgramController.java
│   │   │           │   │   └── RoomController.java
│   │   │           │   ├── application/
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   ├── BuildingServiceImpl.java
│   │   │           │   │   │   ├── DepartmentServiceImpl.java
│   │   │           │   │   │   ├── FacultyServiceImpl.java
│   │   │           │   │   │   ├── ProgramServiceImpl.java
│   │   │           │   │   │   └── RoomServiceImpl.java
│   │   │           │   │   ├── mapper/
│   │   │           │   │   │   ├── BuildingMapper.java
│   │   │           │   │   │   ├── DepartmentMapper.java
│   │   │           │   │   │   ├── FacultyMapper.java
│   │   │           │   │   │   ├── ProgramMapper.java
│   │   │           │   │   │   └── RoomMapper.java
│   │   │           │   │   ├── BuildingService.java
│   │   │           │   │   ├── DepartmentService.java
│   │   │           │   │   ├── FacultyService.java
│   │   │           │   │   ├── ProgramService.java
│   │   │           │   │   └── RoomService.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── Building.java
│   │   │           │   │   ├── Department.java
│   │   │           │   │   ├── Faculty.java
│   │   │           │   │   ├── Program.java
│   │   │           │   │   └── Room.java
│   │   │           │   ├── infrastructure/
│   │   │           │   │   ├── BuildingRepository.java
│   │   │           │   │   ├── DepartmentRepository.java
│   │   │           │   │   ├── FacultyRepository.java
│   │   │           │   │   ├── ProgramRepository.java
│   │   │           │   │   └── RoomRepository.java
│   │   │           │   └── OrganizationPermissions.java
│   │   │           ├── people/
│   │   │           │   ├── api/
│   │   │           │   │   ├── dto/
│   │   │           │   │   │   ├── AddressRequest.java
│   │   │           │   │   │   ├── AddressResponse.java
│   │   │           │   │   │   ├── BatchRequest.java
│   │   │           │   │   │   ├── BatchResponse.java
│   │   │           │   │   │   ├── CountryResponse.java
│   │   │           │   │   │   ├── GeoAreaRequest.java
│   │   │           │   │   │   ├── GeoAreaResponse.java
│   │   │           │   │   │   ├── GeoAreaUpdateRequest.java
│   │   │           │   │   │   ├── GuardianRequest.java
│   │   │           │   │   │   ├── GuardianResponse.java
│   │   │           │   │   │   ├── NewProgramRequest.java
│   │   │           │   │   │   ├── NewStudentResponse.java
│   │   │           │   │   │   ├── PersonRequest.java
│   │   │           │   │   │   ├── PersonResponse.java
│   │   │           │   │   │   ├── PromoteStudentsRequest.java
│   │   │           │   │   │   ├── PromoteStudentsResponse.java
│   │   │           │   │   │   ├── StaffRequest.java
│   │   │           │   │   │   ├── StaffResponse.java
│   │   │           │   │   │   ├── StudentRequest.java
│   │   │           │   │   │   ├── StudentResponse.java
│   │   │           │   │   │   └── YearLevelRequest.java
│   │   │           │   │   ├── BatchController.java
│   │   │           │   │   ├── CountryController.java
│   │   │           │   │   ├── GeoAreaController.java
│   │   │           │   │   ├── GuardianController.java
│   │   │           │   │   ├── PersonController.java
│   │   │           │   │   ├── StaffController.java
│   │   │           │   │   └── StudentController.java
│   │   │           │   ├── application/
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   ├── BatchServiceImpl.java
│   │   │           │   │   │   ├── CountryServiceImpl.java
│   │   │           │   │   │   ├── GeoAreaServiceImpl.java
│   │   │           │   │   │   ├── GuardianServiceImpl.java
│   │   │           │   │   │   ├── PersonAddressServiceImpl.java
│   │   │           │   │   │   ├── PersonServiceImpl.java
│   │   │           │   │   │   ├── StaffServiceImpl.java
│   │   │           │   │   │   └── StudentServiceImpl.java
│   │   │           │   │   ├── mapper/
│   │   │           │   │   │   ├── BatchMapper.java
│   │   │           │   │   │   ├── CountryMapper.java
│   │   │           │   │   │   ├── GeoAreaMapper.java
│   │   │           │   │   │   ├── GuardianMapper.java
│   │   │           │   │   │   ├── PersonMapper.java
│   │   │           │   │   │   ├── StaffMapper.java
│   │   │           │   │   │   └── StudentMapper.java
│   │   │           │   │   ├── BatchService.java
│   │   │           │   │   ├── CountryService.java
│   │   │           │   │   ├── GeoAreaService.java
│   │   │           │   │   ├── GuardianService.java
│   │   │           │   │   ├── PersonAddressService.java
│   │   │           │   │   ├── PersonService.java
│   │   │           │   │   ├── StaffService.java
│   │   │           │   │   └── StudentService.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── Batch.java
│   │   │           │   │   ├── Country.java
│   │   │           │   │   ├── GeoArea.java
│   │   │           │   │   ├── Guardian.java
│   │   │           │   │   ├── Person.java
│   │   │           │   │   ├── PersonAddress.java
│   │   │           │   │   ├── Staff.java
│   │   │           │   │   ├── Student.java
│   │   │           │   │   └── StudentGuardian.java
│   │   │           │   ├── infrastructure/
│   │   │           │   │   ├── BatchRepository.java
│   │   │           │   │   ├── CountryRepository.java
│   │   │           │   │   ├── GeoAreaRepository.java
│   │   │           │   │   ├── GuardianRepository.java
│   │   │           │   │   ├── PersonAddressRepository.java
│   │   │           │   │   ├── PersonRepository.java
│   │   │           │   │   ├── StaffRepository.java
│   │   │           │   │   ├── StudentGuardianRepository.java
│   │   │           │   │   └── StudentRepository.java
│   │   │           │   └── PeoplePermissions.java
│   │   │           ├── request/
│   │   │           │   ├── leave/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── LeaveRequestRequest.java
│   │   │           │   │   │   │   ├── LeaveRequestResponse.java
│   │   │           │   │   │   │   ├── LeaveTypeRequest.java
│   │   │           │   │   │   │   └── LeaveTypeResponse.java
│   │   │           │   │   │   ├── LeaveRequestController.java
│   │   │           │   │   │   └── LeaveTypeController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── LeaveRequestServiceImpl.java
│   │   │           │   │   │   │   └── LeaveTypeServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   ├── LeaveRequestMapper.java
│   │   │           │   │   │   │   └── LeaveTypeMapper.java
│   │   │           │   │   │   ├── LeaveRequestService.java
│   │   │           │   │   │   └── LeaveTypeService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── LeaveRequest.java
│   │   │           │   │   │   ├── LeaveRequestClass.java
│   │   │           │   │   │   └── LeaveType.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── LeaveRequestClassRepository.java
│   │   │           │   │       ├── LeaveRequestRepository.java
│   │   │           │   │       └── LeaveTypeRepository.java
│   │   │           │   ├── studentrequest/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── StudentRequestRequest.java
│   │   │           │   │   │   │   └── StudentRequestResponse.java
│   │   │           │   │   │   └── StudentRequestController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── StudentRequestServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── StudentRequestMapper.java
│   │   │           │   │   │   └── StudentRequestService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── RequestApproval.java
│   │   │           │   │   │   └── StudentRequest.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── RequestApprovalRepository.java
│   │   │           │   │       └── StudentRequestRepository.java
│   │   │           │   └── RequestPermissions.java
│   │   │           ├── security/
│   │   │           │   ├── audit/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── AuditLogRequest.java
│   │   │           │   │   │   │   └── AuditLogResponse.java
│   │   │           │   │   │   └── AuditLogController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── AuditLogServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── AuditLogMapper.java
│   │   │           │   │   │   └── AuditLogService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   └── AuditLog.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       └── AuditLogRepository.java
│   │   │           │   ├── auth/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── ChangePasswordRequest.java
│   │   │           │   │   │   │   ├── CurrentUserResponse.java
│   │   │           │   │   │   │   ├── DeviceInfo.java
│   │   │           │   │   │   │   ├── ForgotPasswordRequest.java
│   │   │           │   │   │   │   ├── LanguageRequest.java
│   │   │           │   │   │   │   ├── LoginRequest.java
│   │   │           │   │   │   │   ├── OtpLoginRequest.java
│   │   │           │   │   │   │   ├── OtpSendRequest.java
│   │   │           │   │   │   │   ├── QrLoginResponse.java
│   │   │           │   │   │   │   ├── RefreshTokenRequest.java
│   │   │           │   │   │   │   ├── RegisterRequest.java
│   │   │           │   │   │   │   ├── ResetPasswordRequest.java
│   │   │           │   │   │   │   └── TokenResponse.java
│   │   │           │   │   │   └── AuthController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── AuthServiceImpl.java
│   │   │           │   │   │   │   ├── JwtServiceImpl.java
│   │   │           │   │   │   │   ├── LogOnlySmsSender.java
│   │   │           │   │   │   │   ├── OtpLoginServiceImpl.java
│   │   │           │   │   │   │   ├── PasswordResetServiceImpl.java
│   │   │           │   │   │   │   ├── PermissionCacheServiceImpl.java
│   │   │           │   │   │   │   ├── QrLoginServiceImpl.java
│   │   │           │   │   │   │   └── ScopeServiceImpl.java
│   │   │           │   │   │   ├── AuthService.java
│   │   │           │   │   │   ├── CustomUserDetailsService.java
│   │   │           │   │   │   ├── JwtService.java
│   │   │           │   │   │   ├── OtpLoginService.java
│   │   │           │   │   │   ├── PasswordResetService.java
│   │   │           │   │   │   ├── PermissionCacheService.java
│   │   │           │   │   │   ├── QrLoginService.java
│   │   │           │   │   │   ├── ScopeService.java
│   │   │           │   │   │   └── SmsSender.java
│   │   │           │   │   └── AuthUser.java
│   │   │           │   ├── filter/
│   │   │           │   │   └── SessionActivityFilter.java
│   │   │           │   ├── menu/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── MenuRequest.java
│   │   │           │   │   │   │   └── MenuResponse.java
│   │   │           │   │   │   └── MenuController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── MenuServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── MenuMapper.java
│   │   │           │   │   │   └── MenuService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   └── Menu.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       └── MenuRepository.java
│   │   │           │   ├── role/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── PermissionResponse.java
│   │   │           │   │   │   │   ├── RoleRequest.java
│   │   │           │   │   │   │   ├── RoleResponse.java
│   │   │           │   │   │   │   └── RoleUpdateRequest.java
│   │   │           │   │   │   ├── PermissionController.java
│   │   │           │   │   │   └── RoleController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── PermissionServiceImpl.java
│   │   │           │   │   │   │   └── RoleServiceImpl.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   ├── PermissionMapper.java
│   │   │           │   │   │   │   └── RoleMapper.java
│   │   │           │   │   │   ├── PermissionService.java
│   │   │           │   │   │   └── RoleService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── Permission.java
│   │   │           │   │   │   └── Role.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── PermissionRepository.java
│   │   │           │   │       └── RoleRepository.java
│   │   │           │   ├── session/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── LoginStatsResponse.java
│   │   │           │   │   │   │   ├── UserLoginStatsResponse.java
│   │   │           │   │   │   │   ├── UserSessionRequest.java
│   │   │           │   │   │   │   └── UserSessionResponse.java
│   │   │           │   │   │   └── UserSessionController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   ├── GeoIpServiceImpl.java
│   │   │           │   │   │   │   └── UserSessionServiceImpl.java
│   │   │           │   │   │   ├── job/
│   │   │           │   │   │   │   └── InactiveSessionCleanupJob.java
│   │   │           │   │   │   ├── listener/
│   │   │           │   │   │   │   └── NewLoginAlertListener.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── UserSessionMapper.java
│   │   │           │   │   │   ├── GeoIpService.java
│   │   │           │   │   │   └── UserSessionService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   └── UserSession.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       └── UserSessionRepository.java
│   │   │           │   ├── user/
│   │   │           │   │   ├── api/
│   │   │           │   │   │   ├── dto/
│   │   │           │   │   │   │   ├── TemporaryPasswordResponse.java
│   │   │           │   │   │   │   ├── UserRequest.java
│   │   │           │   │   │   │   ├── UserResponse.java
│   │   │           │   │   │   │   ├── UserRoleRequest.java
│   │   │           │   │   │   │   └── UserRoleResponse.java
│   │   │           │   │   │   └── UserController.java
│   │   │           │   │   ├── application/
│   │   │           │   │   │   ├── impl/
│   │   │           │   │   │   │   └── UserServiceImpl.java
│   │   │           │   │   │   ├── job/
│   │   │           │   │   │   │   └── FirstAdminSetup.java
│   │   │           │   │   │   ├── mapper/
│   │   │           │   │   │   │   └── UserMapper.java
│   │   │           │   │   │   └── UserService.java
│   │   │           │   │   ├── domain/
│   │   │           │   │   │   ├── User.java
│   │   │           │   │   │   └── UserRole.java
│   │   │           │   │   └── infrastructure/
│   │   │           │   │       ├── UserRepository.java
│   │   │           │   │       └── UserRoleRepository.java
│   │   │           │   └── SecurityPermissions.java
│   │   │           ├── support/
│   │   │           │   ├── api/
│   │   │           │   │   ├── dto/
│   │   │           │   │   │   ├── AnnouncementRequest.java
│   │   │           │   │   │   ├── AnnouncementResponse.java
│   │   │           │   │   │   ├── DocumentRequest.java
│   │   │           │   │   │   ├── DocumentResponse.java
│   │   │           │   │   │   ├── SystemSettingRequest.java
│   │   │           │   │   │   └── SystemSettingResponse.java
│   │   │           │   │   ├── AnnouncementController.java
│   │   │           │   │   ├── DocumentController.java
│   │   │           │   │   └── SystemSettingController.java
│   │   │           │   ├── application/
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   ├── AnnouncementServiceImpl.java
│   │   │           │   │   │   ├── DocumentServiceImpl.java
│   │   │           │   │   │   └── SystemSettingServiceImpl.java
│   │   │           │   │   ├── mapper/
│   │   │           │   │   │   ├── AnnouncementMapper.java
│   │   │           │   │   │   ├── DocumentMapper.java
│   │   │           │   │   │   └── SystemSettingMapper.java
│   │   │           │   │   ├── AnnouncementService.java
│   │   │           │   │   ├── DocumentService.java
│   │   │           │   │   └── SystemSettingService.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── Announcement.java
│   │   │           │   │   ├── Document.java
│   │   │           │   │   └── SystemSetting.java
│   │   │           │   ├── infrastructure/
│   │   │           │   │   ├── AnnouncementRepository.java
│   │   │           │   │   ├── DocumentRepository.java
│   │   │           │   │   └── SystemSettingRepository.java
│   │   │           │   └── SupportPermissions.java
│   │   │           ├── timetable/
│   │   │           │   ├── api/
│   │   │           │   │   ├── dto/
│   │   │           │   │   │   ├── LecturerAvailabilityRequest.java
│   │   │           │   │   │   ├── LecturerAvailabilityResponse.java
│   │   │           │   │   │   ├── TimetableConstraintRequest.java
│   │   │           │   │   │   ├── TimetableConstraintResponse.java
│   │   │           │   │   │   ├── TimetableRunRequest.java
│   │   │           │   │   │   └── TimetableRunResponse.java
│   │   │           │   │   ├── LecturerAvailabilityController.java
│   │   │           │   │   ├── TimetableConstraintController.java
│   │   │           │   │   └── TimetableRunController.java
│   │   │           │   ├── application/
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   ├── LecturerAvailabilityServiceImpl.java
│   │   │           │   │   │   ├── TimetableConstraintServiceImpl.java
│   │   │           │   │   │   └── TimetableRunServiceImpl.java
│   │   │           │   │   ├── mapper/
│   │   │           │   │   │   ├── LecturerAvailabilityMapper.java
│   │   │           │   │   │   ├── TimetableConstraintMapper.java
│   │   │           │   │   │   └── TimetableRunMapper.java
│   │   │           │   │   ├── LecturerAvailabilityService.java
│   │   │           │   │   ├── TimetableConstraintService.java
│   │   │           │   │   └── TimetableRunService.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── LecturerAvailability.java
│   │   │           │   │   ├── TimetableConstraint.java
│   │   │           │   │   ├── TimetableRun.java
│   │   │           │   │   └── TimetableRunItem.java
│   │   │           │   ├── infrastructure/
│   │   │           │   │   ├── LecturerAvailabilityRepository.java
│   │   │           │   │   ├── TimetableConstraintRepository.java
│   │   │           │   │   ├── TimetableRunItemRepository.java
│   │   │           │   │   └── TimetableRunRepository.java
│   │   │           │   └── TimetablePermissions.java
│   │   │           ├── transport/
│   │   │           │   ├── api/
│   │   │           │   │   ├── dto/
│   │   │           │   │   │   ├── TransportRouteRequest.java
│   │   │           │   │   │   ├── TransportRouteResponse.java
│   │   │           │   │   │   ├── TransportSubscriptionRequest.java
│   │   │           │   │   │   ├── TransportSubscriptionResponse.java
│   │   │           │   │   │   ├── VehicleRequest.java
│   │   │           │   │   │   └── VehicleResponse.java
│   │   │           │   │   ├── TransportRouteController.java
│   │   │           │   │   ├── TransportSubscriptionController.java
│   │   │           │   │   └── VehicleController.java
│   │   │           │   ├── application/
│   │   │           │   │   ├── impl/
│   │   │           │   │   │   ├── TransportRouteServiceImpl.java
│   │   │           │   │   │   ├── TransportSubscriptionServiceImpl.java
│   │   │           │   │   │   └── VehicleServiceImpl.java
│   │   │           │   │   ├── mapper/
│   │   │           │   │   │   ├── TransportRouteMapper.java
│   │   │           │   │   │   ├── TransportSubscriptionMapper.java
│   │   │           │   │   │   └── VehicleMapper.java
│   │   │           │   │   ├── TransportRouteService.java
│   │   │           │   │   ├── TransportSubscriptionService.java
│   │   │           │   │   └── VehicleService.java
│   │   │           │   ├── domain/
│   │   │           │   │   ├── RouteStop.java
│   │   │           │   │   ├── TransportRoute.java
│   │   │           │   │   ├── TransportSubscription.java
│   │   │           │   │   └── Vehicle.java
│   │   │           │   ├── infrastructure/
│   │   │           │   │   ├── RouteStopRepository.java
│   │   │           │   │   ├── TransportRouteRepository.java
│   │   │           │   │   ├── TransportSubscriptionRepository.java
│   │   │           │   │   └── VehicleRepository.java
│   │   │           │   └── TransportPermissions.java
│   │   │           └── UmsApplication.java
│   │   └── resources/
│   │       ├── db/
│   │       │   ├── migration/
│   │       │   │   ├── V1__init_schema.sql
│   │       │   │   ├── V2__seed_permissions.sql
│   │       │   │   ├── V3__seed_roles_and_admin.sql
│   │       │   │   ├── V4__seed_geo_areas.sql
│   │       │   │   └── V5__seed_countries.sql
│   │       │   └── testdata/
│   │       │       └── V1000__sample_data.sql
│   │       ├── i18n/
│   │       │   ├── messages.properties        # English (default)
│   │       │   └── messages_km.properties     # Khmer
│   │       ├── application-dev.yml
│   │       ├── application-preprod.yml
│   │       ├── application-prod.yml
│   │       ├── application-sit.yml
│   │       ├── application-uat.yml
│   │       └── application.yml
│   └── test/
│       └── java/
│           └── com/
│               └── ums/
│                   ├── common/
│                   │   └── util/
│                   │       └── PhoneNumbersTest.java
│                   └── ModularityTest.java
├── .env.example
├── .gitignore
├── docker-compose.yml
├── docs/
│   └── Cambodia-Areas.xlsx        # every province, district, commune and village (Khmer + English), from geo_areas
├── build.gradle
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── settings.gradle
├── tools/
│   ├── CountrySync.java           # ./gradlew countrySync: writes a migration when countries change
│   └── GeoSync.java               # ./gradlew geoSync: writes a migration when the ministry updates the areas
├── ums_schema.sql                 # copy of V1__init_schema.sql
├── UMS_V2__seed_permissions.sql   # copy of V2__seed_permissions.sql
├── PROJECT_STRUCTURE.md
└── README.md
```
