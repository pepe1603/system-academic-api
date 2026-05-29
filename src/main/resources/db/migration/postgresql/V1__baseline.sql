-- V1__baseline.sql
-- Generated from production schema dump (Aiven PostgreSQL 17.9)
-- Cleaned for Flyway baseline migration

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;



--
-- Name: fn_calculate_kardex_average(uuid, uuid); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_calculate_kardex_average(p_student_id uuid, p_semester_id uuid DEFAULT NULL::uuid) RETURNS numeric
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_average NUMERIC(5,2);
BEGIN
    IF p_semester_id IS NULL THEN
        SELECT ROUND(AVG(final_grade), 2)
        INTO v_average
        FROM kardex
        WHERE student_id = p_student_id 
          AND status IN ('APPROVED', 'EXTRAORDINARY')
          AND is_deleted = FALSE;
    ELSE
        SELECT ROUND(AVG(final_grade), 2)
        INTO v_average
        FROM kardex
        WHERE student_id = p_student_id 
          AND academic_semester_id = p_semester_id
          AND status IN ('APPROVED', 'EXTRAORDINARY')
          AND is_deleted = FALSE;
    END IF;
    
    RETURN COALESCE(v_average, 0);
END;
$$;


--
-- Name: fn_can_teacher_grade(uuid, uuid); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_can_teacher_grade(p_teacher_id uuid, p_enrollment_id uuid) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_can_grade BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 
        FROM enrollment e
        JOIN academic_group ag ON e.group_id = ag.id
        WHERE e.id = p_enrollment_id
          AND ag.teacher_id = p_teacher_id
    ) INTO v_can_grade;
    
    RETURN v_can_grade;
END;
$$;


--
-- Name: fn_generate_certificate_folio(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_generate_certificate_folio(p_year integer) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_folio VARCHAR(50);
    v_consecutive INTEGER;
BEGIN
    SELECT COALESCE(MAX(
        CAST(SUBSTRING(official_folio FROM 15 FOR 4) AS INTEGER)
    ), 0) + 1 INTO v_consecutive
    FROM certificate
    WHERE official_folio LIKE 'CERT-' || p_year || '-%';
    
    v_folio := 'CERT-' || p_year || '-' || LPAD(v_consecutive::TEXT, 4, '0');
    
    RETURN v_folio;
END;
$$;


--
-- Name: fn_generate_course_code(character varying, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_generate_course_code(p_prefix character varying, p_semester integer) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_consecutive INTEGER;
    v_course_code VARCHAR(20);
BEGIN
    SELECT COALESCE(MAX(
        CAST(SUBSTRING(course_code FROM 4 FOR 3) AS INTEGER)
    ), 0) + 1 INTO v_consecutive
    FROM course
    WHERE course_code LIKE p_prefix || '%';
    
    v_course_code := UPPER(p_prefix) || LPAD(v_consecutive::TEXT, 3, '0');
    
    RETURN v_course_code;
END;
$$;


--
-- Name: fn_generate_employee_number(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_generate_employee_number(p_year integer) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_consecutive INTEGER;
    v_employee_number VARCHAR(20);
BEGIN
    SELECT COALESCE(MAX(
        CAST(SUBSTRING(employee_number FROM 9 FOR 4) AS INTEGER)
    ), 0) + 1 INTO v_consecutive
    FROM teacher
    WHERE employee_number LIKE 'EMP-' || p_year || '-%';
    
    v_employee_number := 'EMP-' || p_year || '-' || LPAD(v_consecutive::TEXT, 4, '0');
    
    RETURN v_employee_number;
END;
$$;


--
-- Name: fn_generate_enrollment_number(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_generate_enrollment_number(p_year integer) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_consecutive INTEGER;
    v_enrollment_number VARCHAR(20);
BEGIN
    SELECT COALESCE(MAX(
        CAST(SUBSTRING(enrollment_number FROM 9 FOR 4) AS INTEGER)
    ), 0) + 1 INTO v_consecutive
    FROM student
    WHERE enrollment_number LIKE 'ENE-' || p_year || '-%';
    
    v_enrollment_number := 'ENE-' || p_year || '-' || LPAD(v_consecutive::TEXT, 4, '0');
    
    RETURN v_enrollment_number;
END;
$$;


--
-- Name: fn_generate_kardex_folio(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_generate_kardex_folio() RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_folio VARCHAR(30);
    v_consecutive INTEGER;
BEGIN
    SELECT COALESCE(MAX(
        CAST(SUBSTRING(official_folio FROM 15 FOR 4) AS INTEGER)
    ), 0) + 1 INTO v_consecutive
    FROM kardex
    WHERE official_folio LIKE 'KX-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-%';
    
    v_folio := 'KX-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(v_consecutive::TEXT, 4, '0');
    
    RETURN v_folio;
END;
$$;


--
-- Name: fn_generate_report_card_folio(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_generate_report_card_folio(p_year integer) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_folio VARCHAR(30);
    v_consecutive INTEGER;
BEGIN
    SELECT COALESCE(MAX(
        CAST(SUBSTRING(folio FROM 10 FOR 4) AS INTEGER)
    ), 0) + 1 INTO v_consecutive
    FROM report_card
    WHERE folio LIKE 'RC-' || p_year || '-%';
    
    v_folio := 'RC-' || p_year || '-' || LPAD(v_consecutive::TEXT, 4, '0');
    
    RETURN v_folio;
END;
$$;


--
-- Name: fn_get_group_students(uuid); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_get_group_students(p_teacher_id uuid) RETURNS TABLE(student_id uuid, enrollment_number character varying, full_name character varying, course_name character varying, group_name character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.id AS student_id,
        s.enrollment_number,
        s.first_name || ' ' || s.last_name AS full_name,
        c.name AS course_name,
        ag.name AS group_name
    FROM enrollment e
    JOIN student s ON e.student_id = s.id
    JOIN course c ON e.course_id = c.id
    JOIN academic_group ag ON e.group_id = ag.id
    WHERE ag.teacher_id = p_teacher_id
      AND e.is_deleted = FALSE
      AND s.is_deleted = FALSE;
END;
$$;


--
-- Name: fn_grade_to_letter(numeric); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_grade_to_letter(p_grade numeric) RETURNS character varying
    LANGUAGE plpgsql IMMUTABLE
    AS $$
BEGIN
    CASE
        WHEN p_grade >= 90 THEN RETURN 'A';
        WHEN p_grade >= 80 THEN RETURN 'B';
        WHEN p_grade >= 70 THEN RETURN 'C';
        WHEN p_grade >= 60 THEN RETURN 'D';
        WHEN p_grade < 60 THEN RETURN 'F';
        ELSE RETURN 'N/A';
    END CASE;
END;
$$;


--
-- Name: fn_only_one_current_semester(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_only_one_current_semester() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NEW.is_current = TRUE THEN
        UPDATE academic_semester SET is_current = FALSE WHERE id != NEW.id AND is_current = TRUE;
    END IF;
    RETURN NEW;
END;
$$;


--
-- Name: fn_set_updated_at(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_set_updated_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;


--
-- Name: fn_soft_delete(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_soft_delete() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.is_deleted = TRUE;
    NEW.is_active = FALSE;
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;


--
-- Name: fn_update_academic_group(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_update_academic_group() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;


--
-- Name: fn_validate_course_weight(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_validate_course_weight() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    total_weight NUMERIC(5,2);
BEGIN
    SELECT COALESCE(SUM(weight),0)
    INTO total_weight
    FROM evaluation_type
    WHERE course_id = NEW.course_id
      AND is_active = TRUE;

    IF total_weight > 100 THEN
        RAISE EXCEPTION 
        'El peso total de evaluación excede el 100%% (Total actual: %)',
        total_weight;
    END IF;

    RETURN NEW;
END;
$$;


--
-- Name: sp_assign_teacher_to_group(uuid, uuid); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_assign_teacher_to_group(p_group_id uuid, p_teacher_id uuid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE academic_group 
    SET teacher_id = p_teacher_id, updated_at = now()
    WHERE id = p_group_id;
END;
$$;


--
-- Name: sp_close_academic_semester(uuid, boolean); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_close_academic_semester(p_semester_id uuid, p_apply_averages boolean DEFAULT true) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_enrollment RECORD;
    v_average NUMERIC(5,2);
BEGIN
    UPDATE academic_semester SET status = 'CLOSED', is_current = FALSE, updated_at = now() WHERE id = p_semester_id;
    
    IF p_apply_averages THEN
        FOR v_enrollment IN
            SELECT e.id, e.student_id, e.course_id
            FROM enrollment e
            WHERE e.is_deleted = FALSE
        LOOP
            SELECT ROUND(SUM(g.score * et.weight / 100), 2)
            INTO v_average
            FROM grade g
            JOIN evaluation_type et ON g.evaluation_type_id = et.id
            WHERE g.enrollment_id = v_enrollment.id;
            
            UPDATE kardex
            SET final_grade = v_average,
                letter_grade = fn_grade_to_letter(v_average),
                status = CASE WHEN v_average >= 60 THEN 'APPROVED' ELSE 'FAILED' END,
                approval_date = CURRENT_DATE,
                updated_at = now()
            WHERE student_id = v_enrollment.student_id
              AND course_id = v_enrollment.course_id
              AND academic_semester_id = p_semester_id;
        END LOOP;
    END IF;
    
    RAISE NOTICE 'Semestre académico cerrado exitosamente';
END;
$$;


--
-- Name: sp_enroll_in_course(uuid, uuid, uuid, uuid, uuid); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_enroll_in_course(p_student_id uuid, p_course_id uuid, p_period_id uuid, p_group_id uuid, p_semester_id uuid) RETURNS uuid
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_enrollment_id UUID;
    v_kardex_id UUID;
BEGIN
    v_enrollment_id := sp_enroll_student(p_student_id, p_course_id, p_period_id, p_group_id);
    
    INSERT INTO kardex (id, student_id, course_id, academic_semester_id, enrollment_id, status, attempt_number, enrollment_date, created_at)
    VALUES (uuid_generate_v4(), p_student_id, p_course_id, p_semester_id, v_enrollment_id, 'ENROLLED', 1, CURRENT_DATE, now())
    RETURNING id INTO v_kardex_id;
    
    RETURN v_enrollment_id;
END;
$$;


--
-- Name: sp_enroll_student(uuid, uuid, uuid, uuid); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_enroll_student(p_student_id uuid, p_course_id uuid, p_period_id uuid, p_group_id uuid DEFAULT NULL::uuid) RETURNS uuid
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_enrollment_id UUID;
BEGIN
    IF EXISTS (
        SELECT 1
        FROM enrollment
        WHERE student_id = p_student_id
          AND course_id = p_course_id
          AND academic_period_id = p_period_id
          AND is_deleted = FALSE
    ) THEN
        RAISE EXCEPTION 'El estudiante ya está inscrito en esta materia para este período académico';
    END IF;

    INSERT INTO enrollment (id, student_id, course_id, academic_period_id, group_id, status, is_active, is_deleted, created_at)
    VALUES (uuid_generate_v4(), p_student_id, p_course_id, p_period_id, p_group_id, 'ENROLLED', TRUE, FALSE, now())
    RETURNING id INTO v_enrollment_id;

    RETURN v_enrollment_id;
END;
$$;


--
-- Name: sp_generate_official_report_card(uuid, uuid, character varying, uuid); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_generate_official_report_card(p_student_id uuid, p_academic_semester_id uuid, p_report_card_type character varying DEFAULT 'ORDINARY'::character varying, p_signed_by uuid DEFAULT NULL::uuid) RETURNS uuid
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_report_card_id UUID;
BEGIN
    v_report_card_id := sp_generate_report_card(
        p_student_id, 
        p_academic_semester_id, 
        p_report_card_type
    );
    
    UPDATE report_card 
    SET generation_mode = 'OFFICIAL', 
        status = 'ISSUED',
        is_signed = TRUE,
        signed_by = p_signed_by,
        signed_at = now()
    WHERE id = v_report_card_id;
    
    RETURN v_report_card_id;
END;
$$;


--
-- Name: sp_generate_online_report_card(uuid, uuid); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_generate_online_report_card(p_student_id uuid, p_academic_semester_id uuid) RETURNS uuid
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_report_card_id UUID;
BEGIN
    v_report_card_id := sp_generate_report_card(
        p_student_id, 
        p_academic_semester_id, 
        'ORDINARY'
    );
    
    UPDATE report_card 
    SET generation_mode = 'ONLINE', status = 'ISSUED'
    WHERE id = v_report_card_id;
    
    RETURN v_report_card_id;
END;
$$;


--
-- Name: sp_generate_report_card(uuid, uuid, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_generate_report_card(p_student_id uuid, p_academic_semester_id uuid, p_report_card_type character varying DEFAULT 'ORDINARY'::character varying) RETURNS uuid
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_report_card_id UUID;
    v_average NUMERIC(5,2);
    v_credits_approved INTEGER := 0;
    v_subjects_approved INTEGER := 0;
    v_total_subjects INTEGER := 0;
    v_generation_id UUID;
    v_year INTEGER;
BEGIN
    SELECT generation_id INTO v_generation_id FROM student WHERE id = p_student_id;
    SELECT EXTRACT(YEAR FROM issue_date)::INTEGER INTO v_year FROM academic_semester WHERE id = p_academic_semester_id;
    
    v_average := fn_calculate_kardex_average(p_student_id, p_academic_semester_id);
    
    SELECT COUNT(*), 
           COUNT(CASE WHEN status IN ('APPROVED', 'EXTRAORDINARY') THEN 1 END),
           COALESCE(SUM(CASE WHEN status IN ('APPROVED', 'EXTRAORDINARY') THEN c.credits ELSE 0 END), 0)
    INTO v_total_subjects, v_subjects_approved, v_credits_approved
    FROM kardex k
    JOIN course c ON k.course_id = c.id
    WHERE k.student_id = p_student_id
      AND k.academic_semester_id = p_academic_semester_id
      AND k.is_deleted = FALSE;
    
    INSERT INTO report_card (id, student_id, academic_semester_id, generation_id, report_card_type, overall_average, average_letter, total_subjects, total_subjects_approved, total_credits_enrolled, total_credits_approved, issue_date, folio, status, created_at)
    VALUES (
        uuid_generate_v4(), 
        p_student_id, 
        p_academic_semester_id, 
        v_generation_id, 
        p_report_card_type, 
        v_average, 
        fn_grade_to_letter(v_average), 
        v_total_subjects, 
        v_subjects_approved,
        (SELECT SUM(credits) FROM kardex k2 JOIN course c2 ON k2.course_id = c2.id WHERE k2.student_id = p_student_id AND k2.academic_semester_id = p_academic_semester_id), 
        v_credits_approved, 
        CURRENT_DATE, 
        fn_generate_report_card_folio(v_year), 
        'ISSUED', 
        now()
    )
    RETURNING id INTO v_report_card_id;
    
    INSERT INTO report_card_detail (id, report_card_id, kardex_id, course_id, subject_name, subject_code, credits, grade, grade_letter, subject_status)
    SELECT uuid_generate_v4(), v_report_card_id, k.id, c.id, c.name, c.course_code, c.credits, k.final_grade, fn_grade_to_letter(k.final_grade), k.status
    FROM kardex k
    JOIN course c ON k.course_id = c.id
    WHERE k.student_id = p_student_id
      AND k.academic_semester_id = p_academic_semester_id
      AND k.is_deleted = FALSE;
    
    RETURN v_report_card_id;
END;
$$;


--
-- Name: sp_log_access(uuid, character varying, character varying, character varying, boolean, jsonb); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_log_access(p_user_id uuid, p_action character varying, p_module character varying, p_ip character varying, p_success boolean, p_metadata jsonb) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO access_audit (id, user_id, action, module, ip_address, success, metadata, created_at)
    VALUES (uuid_generate_v4(), p_user_id, p_action, p_module, p_ip, p_success, p_metadata, now());
END;
$$;




--
-- Name: academic_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.academic_group (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(50) NOT NULL,
    academic_semester_id uuid,
    course_id uuid,
    teacher_id uuid,
    capacity integer DEFAULT 30,
    is_active boolean DEFAULT true,
    is_deleted boolean DEFAULT false,
    created_at date DEFAULT now(),
    updated_at date
);


--
-- Name: academic_period; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.academic_period (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(20) NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    is_active boolean DEFAULT true,
    is_deleted boolean DEFAULT false,
    created_at date DEFAULT now(),
    updated_at date,
    CONSTRAINT academic_period_check CHECK ((end_date > start_date))
);


--
-- Name: academic_semester; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.academic_semester (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(50) NOT NULL,
    year integer NOT NULL,
    period integer NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    classes_start_date date NOT NULL,
    classes_end_date date NOT NULL,
    enrollment_deadline date,
    drop_deadline date,
    status character varying(20) DEFAULT 'DRAFT'::character varying,
    is_current boolean DEFAULT false,
    is_deleted boolean DEFAULT false,
    created_at date DEFAULT now(),
    updated_at date,
    CONSTRAINT academic_semester_period_check CHECK ((period = ANY (ARRAY[1, 2]))),
    CONSTRAINT academic_semester_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'OPEN'::character varying, 'CLOSED'::character varying, 'ARCHIVED'::character varying])::text[]))),
    CONSTRAINT academic_semester_year_check CHECK ((year >= 2000)),
    CONSTRAINT chk_semester_dates CHECK (((end_date > start_date) AND (classes_end_date >= classes_start_date)))
);


--
-- Name: access_audit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.access_audit (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    user_id uuid,
    action character varying(100),
    module character varying(100),
    ip_address character varying(45),
    success boolean,
    metadata jsonb,
    created_at timestamp with time zone DEFAULT now()
);


--
-- Name: app_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_user (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    username character varying(50) NOT NULL,
    email character varying(150) NOT NULL,
    password_hash character varying(255) NOT NULL,
    failed_attempts integer DEFAULT 0,
    is_locked boolean DEFAULT false,
    last_login timestamp with time zone,
    is_active boolean DEFAULT true,
    is_deleted boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone,
    created_by uuid,
    updated_by uuid,
    password_changed_at timestamp with time zone,
    must_change_password boolean DEFAULT false,
    two_factor_enabled boolean DEFAULT false,
    two_factor_secret character varying(255),
    two_factor_backup_codes text,
    is_verified boolean DEFAULT false,
    verified_at timestamp with time zone,
    temp_password character varying(255),
    must_verify_email boolean DEFAULT false,
    CONSTRAINT app_user_failed_attempts_check CHECK ((failed_attempts >= 0))
);


--
-- Name: attendance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.attendance (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    enrollment_id uuid NOT NULL,
    attendance_date date NOT NULL,
    status character varying(20) DEFAULT 'PRESENT'::character varying,
    class_time character varying(10),
    subject_code character varying(20),
    observations text,
    justified_by uuid,
    justification_date date,
    recorded_by uuid,
    recorded_at date DEFAULT now(),
    is_deleted boolean,
    CONSTRAINT attendance_status_check CHECK (((status)::text = ANY ((ARRAY['PRESENT'::character varying, 'ABSENT'::character varying, 'JUSTIFIED'::character varying, 'LATE'::character varying])::text[])))
);


--
-- Name: attendance_period; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.attendance_period (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    enrollment_id uuid NOT NULL,
    academic_semester_id uuid NOT NULL,
    total_classes integer DEFAULT 0,
    total_present integer DEFAULT 0,
    total_absent integer DEFAULT 0,
    total_justified integer DEFAULT 0,
    total_late integer DEFAULT 0,
    attendance_percentage numeric(5,2),
    attendance_status character varying(20) DEFAULT 'IN_RANGE'::character varying,
    observations text,
    updated_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now()
);


--
-- Name: certificate; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.certificate (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    student_id uuid NOT NULL,
    generation_id uuid,
    certificate_type character varying(50) NOT NULL,
    official_folio character varying(50),
    internal_folio character varying(50),
    series character varying(20),
    final_average numeric(5,2),
    total_credits integer,
    total_subjects integer,
    issue_date date NOT NULL,
    delivery_date date,
    status character varying(20) DEFAULT 'ISSUED'::character varying,
    director_signer uuid,
    secretary_signer uuid,
    record_number character varying(30),
    record_book character varying(20),
    record_page character varying(20),
    observations text,
    is_deleted boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone,
    CONSTRAINT certificate_certificate_type_check CHECK (((certificate_type)::text = ANY ((ARRAY['PARTIAL'::character varying, 'TOTAL'::character varying, 'TITLE'::character varying, 'DIPLOMA'::character varying, 'CONSTANCIA'::character varying])::text[]))),
    CONSTRAINT certificate_status_check CHECK (((status)::text = ANY ((ARRAY['REQUESTED'::character varying, 'IN_PROCESS'::character varying, 'ISSUED'::character varying, 'DELIVERED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: conduct; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conduct (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    enrollment_id uuid NOT NULL,
    academic_semester_id uuid NOT NULL,
    grade character varying(2),
    observations text,
    warnings integer DEFAULT 0,
    congratulations integer DEFAULT 0,
    registration_date date DEFAULT now(),
    recorded_by uuid,
    updated_at date,
    is_deleted boolean
);


--
-- Name: conduct_incident; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conduct_incident (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    enrollment_id uuid NOT NULL,
    incident_type character varying(50) NOT NULL,
    description text NOT NULL,
    incident_date date NOT NULL,
    severity character varying(20) DEFAULT 'MINOR'::character varying,
    actions_taken text,
    attention_date date,
    recorded_by uuid,
    created_at date DEFAULT now(),
    is_deleted boolean,
    CONSTRAINT conduct_incident_incident_type_check CHECK (((incident_type)::text = ANY ((ARRAY['WARNING'::character varying, 'CONGRATULATION'::character varying, 'CALL_ATTENTION'::character varying, 'SUSPENSION'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT conduct_incident_severity_check CHECK (((severity)::text = ANY ((ARRAY['MINOR'::character varying, 'MODERATE'::character varying, 'SERIOUS'::character varying])::text[])))
);


--
-- Name: course; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.course (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    study_plan_id uuid,
    semester_id uuid,
    course_code character varying(20) NOT NULL,
    name character varying(150) NOT NULL,
    credits integer NOT NULL,
    hours_theory integer DEFAULT 0,
    hours_practice integer DEFAULT 0,
    description text,
    is_mandatory boolean DEFAULT true,
    is_active boolean DEFAULT true,
    is_deleted boolean DEFAULT false,
    created_at date DEFAULT now(),
    updated_at date,
    CONSTRAINT course_credits_check CHECK ((credits > 0))
);


--
-- Name: educational_resource; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.educational_resource (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    title character varying(200) NOT NULL,
    description text,
    resource_type character varying(50) NOT NULL,
    resource_url text NOT NULL,
    course_id uuid,
    is_published boolean DEFAULT true,
    is_deleted boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone,
    CONSTRAINT educational_resource_resource_type_check CHECK (((resource_type)::text = ANY ((ARRAY['PDF'::character varying, 'VIDEO'::character varying, 'LINK'::character varying, 'DOCUMENT'::character varying, 'PRESENTATION'::character varying])::text[])))
);


--
-- Name: email_verification; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.email_verification (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    user_id uuid NOT NULL,
    verification_code character varying(6) NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    is_verified boolean DEFAULT false,
    verified_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now()
);


--
-- Name: enrollment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.enrollment (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    student_id uuid NOT NULL,
    course_id uuid NOT NULL,
    academic_period_id uuid NOT NULL,
    group_id uuid,
    status character varying(30) DEFAULT 'ENROLLED'::character varying,
    is_active boolean DEFAULT true,
    is_deleted boolean DEFAULT false,
    created_at date DEFAULT now(),
    updated_at date,
    CONSTRAINT enrollment_status_check CHECK (((status)::text = ANY ((ARRAY['ENROLLED'::character varying, 'APPROVED'::character varying, 'FAILED'::character varying, 'WITHDRAWN'::character varying])::text[])))
);


--
-- Name: evaluation_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.evaluation_type (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    course_id uuid,
    code character varying(20) NOT NULL,
    name character varying(50),
    weight numeric(5,2),
    is_active boolean DEFAULT true,
    created_at date DEFAULT now(),
    CONSTRAINT evaluation_type_weight_check CHECK (((weight >= (0)::numeric) AND (weight <= (100)::numeric)))
);


--
-- Name: extraordinary_exam; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.extraordinary_exam (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    student_id uuid NOT NULL,
    course_id uuid NOT NULL,
    academic_semester_id uuid,
    attempt_number integer DEFAULT 1 NOT NULL,
    status character varying(20) DEFAULT 'SCHEDULED'::character varying,
    scheduled_date date,
    application_date date,
    application_time character varying(10),
    application_location character varying(100),
    previous_grade numeric(5,2),
    grade numeric(5,2),
    grade_letter character varying(2),
    examiner_id uuid,
    observation text,
    cost numeric(10,2) DEFAULT 0,
    payment_receipt character varying(100),
    payment_folio character varying(50),
    is_deleted boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone,
    CONSTRAINT extraordinary_exam_status_check CHECK (((status)::text = ANY ((ARRAY['SCHEDULED'::character varying, 'APPLIED'::character varying, 'APPROVED'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying, 'NO_SHOW'::character varying])::text[])))
);


--
-- Name: generation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.generation (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(100) NOT NULL,
    entry_year integer NOT NULL,
    graduation_year integer,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    start_date date NOT NULL,
    end_date date,
    is_deleted boolean DEFAULT false,
    created_at date DEFAULT now(),
    updated_at date,
    is_active boolean,
    CONSTRAINT generation_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'GRADUATED'::character varying, 'ARCHIVED'::character varying])::text[])))
);


--
-- Name: grade; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.grade (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    enrollment_id uuid,
    evaluation_type_id uuid,
    score numeric(5,2),
    recorded_by uuid,
    recorded_at date DEFAULT now(),
    is_deleted boolean,
    CONSTRAINT grade_score_check CHECK (((score >= (0)::numeric) AND (score <= (100)::numeric)))
);


--
-- Name: guardian; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.guardian (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    student_id uuid NOT NULL,
    full_name character varying(200) NOT NULL,
    relationship character varying(50) NOT NULL,
    curp character varying(18),
    primary_phone character varying(20),
    secondary_phone character varying(20),
    email character varying(150),
    occupation character varying(100),
    company character varying(150),
    address text,
    is_emergency_contact boolean DEFAULT true,
    is_active boolean DEFAULT true,
    is_deleted boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone,
    CONSTRAINT guardian_relationship_check CHECK (((relationship)::text = ANY ((ARRAY['FATHER'::character varying, 'MOTHER'::character varying, 'GUARDIAN'::character varying, 'SIBLING'::character varying, 'OTHER'::character varying])::text[])))
);


--
-- Name: kardex; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.kardex (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    student_id uuid NOT NULL,
    course_id uuid NOT NULL,
    academic_semester_id uuid NOT NULL,
    enrollment_id uuid,
    final_grade numeric(5,2),
    letter_grade character varying(2),
    status character varying(20) DEFAULT 'ENROLLED'::character varying NOT NULL,
    attempt_number integer DEFAULT 1,
    enrollment_date date NOT NULL,
    approval_date date,
    registration_date timestamp with time zone DEFAULT now(),
    official_folio character varying(30),
    kardex_folio character varying(30),
    kardex_sequence integer,
    is_officialized boolean DEFAULT false,
    officialization_date timestamp with time zone,
    officialized_by uuid,
    observations text,
    is_deleted boolean DEFAULT false,
    created_at date DEFAULT now(),
    updated_at date,
    CONSTRAINT kardex_final_grade_check CHECK (((final_grade >= (0)::numeric) AND (final_grade <= (100)::numeric))),
    CONSTRAINT kardex_status_check CHECK (((status)::text = ANY ((ARRAY['ENROLLED'::character varying, 'APPROVED'::character varying, 'FAILED'::character varying, 'EXTRAORDINARY'::character varying, 'DROPPED'::character varying, 'VALIDATED'::character varying, 'EQUIVALENCE'::character varying])::text[])))
);


--
-- Name: password_recovery; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.password_recovery (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    user_id uuid,
    recovery_token character varying(255) NOT NULL,
    is_used boolean DEFAULT false,
    expires_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);


--
-- Name: permission; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.permission (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying(100) NOT NULL,
    description text,
    module character varying(100),
    is_active boolean DEFAULT true,
    created_at timestamp with time zone DEFAULT now()
);


--
-- Name: registration_request; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.registration_request (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    curp character varying(18) NOT NULL,
    email character varying(150) NOT NULL,
    student_id uuid,
    teacher_id uuid,
    status character varying(20) DEFAULT 'PENDING'::character varying,
    otp_code character varying(6) NOT NULL,
    otp_expires_at timestamp with time zone NOT NULL,
    requested_at timestamp with time zone DEFAULT now(),
    processed_at timestamp with time zone,
    processed_by uuid,
    rejection_reason text,
    notes text,
    CONSTRAINT registration_request_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'EXPIRED'::character varying])::text[])))
);


--
-- Name: report_card; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.report_card (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    student_id uuid NOT NULL,
    academic_semester_id uuid NOT NULL,
    generation_id uuid,
    report_card_type character varying(30) DEFAULT 'ORDINARY'::character varying NOT NULL,
    generation_mode character varying(20) DEFAULT 'ONLINE'::character varying NOT NULL,
    overall_average numeric(5,2),
    average_letter character varying(2),
    attendance_average numeric(5,2),
    total_credits_enrolled integer DEFAULT 0,
    total_credits_approved integer DEFAULT 0,
    total_subjects integer DEFAULT 0,
    total_subjects_approved integer DEFAULT 0,
    status character varying(20) DEFAULT 'ISSUED'::character varying,
    issue_date date NOT NULL,
    delivery_date date,
    origin_semester_id uuid,
    destination_semester_id uuid,
    folio character varying(30),
    series character varying(20),
    observations text,
    is_signed boolean DEFAULT false,
    signed_by uuid,
    signed_at date,
    signed_seal_url text,
    is_deleted boolean DEFAULT false,
    created_at date DEFAULT now(),
    updated_at date,
    CONSTRAINT report_card_generation_mode_check CHECK (((generation_mode)::text = ANY ((ARRAY['ONLINE'::character varying, 'OFFICIAL'::character varying])::text[]))),
    CONSTRAINT report_card_overall_average_check CHECK (((overall_average >= (0)::numeric) AND (overall_average <= (100)::numeric))),
    CONSTRAINT report_card_report_card_type_check CHECK (((report_card_type)::text = ANY ((ARRAY['ORDINARY'::character varying, 'EXTRAORDINARY'::character varying, 'SPECIAL'::character varying, 'PARTIAL_CERTIFICATE'::character varying, 'FINAL_CERTIFICATE'::character varying])::text[]))),
    CONSTRAINT report_card_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ISSUED'::character varying, 'DELIVERED'::character varying, 'ARCHIVED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: report_card_detail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.report_card_detail (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    report_card_id uuid NOT NULL,
    kardex_id uuid,
    course_id uuid NOT NULL,
    subject_name character varying(150) NOT NULL,
    subject_code character varying(20) NOT NULL,
    credits integer NOT NULL,
    grade numeric(5,2),
    grade_letter character varying(2),
    subject_status character varying(20),
    attendance_percentage numeric(5,2),
    total_attendances integer,
    classes_attended integer,
    observations text,
    created_at date DEFAULT now(),
    CONSTRAINT report_card_detail_grade_check CHECK (((grade >= (0)::numeric) AND (grade <= (100)::numeric)))
);


--
-- Name: retake_exam; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.retake_exam (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    student_id uuid NOT NULL,
    course_id uuid NOT NULL,
    academic_semester_id uuid NOT NULL,
    origin_semester_id uuid,
    previous_average numeric(5,2),
    status character varying(20) DEFAULT 'ENROLLED'::character varying,
    is_deleted boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone
);


--
-- Name: role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(50) NOT NULL,
    description text,
    is_active boolean DEFAULT true,
    is_deleted boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone
);


--
-- Name: role_permission; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role_permission (
    role_id uuid NOT NULL,
    permission_id uuid NOT NULL
);


--
-- Name: semester; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.semester (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    study_plan_id uuid,
    semester_number integer NOT NULL,
    name character varying(50) NOT NULL,
    is_active boolean DEFAULT true,
    created_at timestamp(6) without time zone DEFAULT now(),
    is_deleted boolean,
    updated_at timestamp(6) without time zone,
    CONSTRAINT semester_semester_number_check CHECK (((semester_number >= 1) AND (semester_number <= 10)))
);


--
-- Name: student; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    user_id uuid,
    enrollment_number character varying(20) NOT NULL,
    curp character varying(18) NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    institutional_email character varying(150),
    secondary_email character varying(150),
    phone character varying(20),
    secondary_phone character varying(20),
    birth_date date,
    gender character varying(1),
    enrollment_date date,
    graduation_date date,
    marital_status character varying(20),
    birth_place character varying(200),
    nationality character varying(50) DEFAULT 'MEXICANA'::character varying,
    address_street character varying(200),
    address_colony character varying(100),
    address_municipality character varying(100),
    address_state character varying(100),
    address_zip_code character varying(10),
    blood_type character varying(5),
    previous_school text,
    photo_url text,
    observations text,
    has_scholarship boolean DEFAULT false,
    scholarship_type character varying(50),
    generation_id uuid,
    is_active boolean DEFAULT true,
    is_deleted boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone,
    CONSTRAINT student_gender_check CHECK (((gender)::bpchar = ANY (ARRAY['M'::bpchar, 'F'::bpchar, 'O'::bpchar])))
);


--
-- Name: student_document; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_document (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    student_id uuid NOT NULL,
    document_type character varying(50) NOT NULL,
    original_name character varying(200) NOT NULL,
    file_name character varying(200) NOT NULL,
    file_path text NOT NULL,
    file_size_bytes bigint,
    mime_type character varying(100),
    document_number character varying(50),
    issue_date date,
    expiration_date date,
    is_verified boolean DEFAULT false,
    verified_by uuid,
    verification_date timestamp with time zone,
    observations text,
    is_active boolean DEFAULT true,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone,
    is_deleted boolean,
    CONSTRAINT student_document_document_type_check CHECK (((document_type)::text = ANY ((ARRAY['CURP'::character varying, 'BIRTH_CERTIFICATE'::character varying, 'PHOTO'::character varying, 'HIGH_SCHOOL_CERTIFICATE'::character varying, 'HIGH_SCHOOL_KARDEX'::character varying, 'IDENTIFICATION'::character varying, 'PROOF_OF_ADDRESS'::character varying, 'PAYMENT'::character varying, 'OTHER'::character varying])::text[])))
);


--
-- Name: study_plan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.study_plan (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying(20) NOT NULL,
    name character varying(150) NOT NULL,
    version character varying(20),
    description text,
    title_degree character varying(150),
    total_credits integer,
    duration_semesters integer,
    is_active boolean DEFAULT true,
    is_deleted boolean DEFAULT false,
    created_at date DEFAULT now(),
    updated_at date
);


--
-- Name: system_configuration; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.system_configuration (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    config_key character varying(50) NOT NULL,
    config_value text NOT NULL,
    description text,
    data_type character varying(20) DEFAULT 'STRING'::character varying,
    module character varying(50),
    is_active boolean DEFAULT true,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone,
    is_deleted boolean,
    CONSTRAINT system_configuration_data_type_check CHECK (((data_type)::text = ANY ((ARRAY['STRING'::character varying, 'NUMBER'::character varying, 'BOOLEAN'::character varying, 'JSON'::character varying])::text[])))
);


--
-- Name: teacher; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.teacher (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    user_id uuid,
    employee_number character varying(20),
    rfc character varying(13),
    curp character varying(18),
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    institutional_email character varying(150),
    secondary_email character varying(150),
    phone character varying(20),
    secondary_phone character varying(20),
    is_active boolean DEFAULT true,
    is_deleted boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone
);


--
-- Name: user_profile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_profile (
    id uuid NOT NULL,
    address text,
    birth_date date,
    city character varying(100),
    created_at timestamp(6) without time zone NOT NULL,
    curp character varying(18),
    employee_number character varying(20),
    enrollment_number character varying(20),
    first_name character varying(100) NOT NULL,
    gender character varying(1),
    institutional_email character varying(150),
    is_active boolean,
    is_deleted boolean,
    last_name character varying(100) NOT NULL,
    phone character varying(20),
    postal_code character varying(10),
    profile_picture_url character varying(500),
    rfc character varying(13),
    secondary_email character varying(150),
    secondary_phone character varying(20),
    state character varying(100),
    updated_at timestamp(6) without time zone,
    user_id uuid
);


--
-- Name: user_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_role (
    user_id uuid NOT NULL,
    role_id uuid NOT NULL
);


--
-- Name: user_session; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_session (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    user_id uuid,
    jwt_token text NOT NULL,
    ip_address character varying(45),
    user_agent text,
    started_at timestamp with time zone DEFAULT now(),
    expires_at timestamp with time zone,
    is_active boolean DEFAULT true,
    refresh_token text
);


--
-- Name: v_asistencia_concentrado; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_asistencia_concentrado AS
 SELECT s.enrollment_number,
    (((s.first_name)::text || ' '::text) || (s.last_name)::text) AS student_name,
    c.course_code,
    c.name AS course_name,
    ac.name AS semester,
    ap.total_classes,
    ap.total_present AS present,
    ap.total_absent AS absent,
    ap.attendance_percentage AS percentage,
        CASE
            WHEN (ap.attendance_percentage >= (80)::numeric) THEN 'SATISFACTORY'::text
            WHEN (ap.attendance_percentage >= (60)::numeric) THEN 'AT_RISK'::text
            ELSE 'INSUFFICIENT'::text
        END AS attendance_status
   FROM ((((public.attendance_period ap
     JOIN public.enrollment e ON ((ap.enrollment_id = e.id)))
     JOIN public.student s ON ((e.student_id = s.id)))
     JOIN public.course c ON ((e.course_id = c.id)))
     JOIN public.academic_semester ac ON ((ap.academic_semester_id = ac.id)))
  WHERE (e.is_deleted = false)
  ORDER BY ac.year DESC, s.last_name;


--
-- Name: v_boletas_periodo; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_boletas_periodo AS
 SELECT rc.id AS report_card_id,
    s.enrollment_number,
    (((s.first_name)::text || ' '::text) || (s.last_name)::text) AS student_name,
    ac.name AS academic_semester,
    rc.report_card_type AS type,
    rc.overall_average AS average,
    rc.average_letter,
    rc.total_subjects,
    rc.total_subjects_approved AS subjects_approved,
    rc.total_credits_approved AS credits_approved,
    rc.issue_date,
    rc.status,
    rc.folio
   FROM ((public.report_card rc
     JOIN public.student s ON ((rc.student_id = s.id)))
     JOIN public.academic_semester ac ON ((rc.academic_semester_id = ac.id)))
  WHERE (rc.is_deleted = false)
  ORDER BY ac.year DESC, ac.period DESC, s.last_name;


--
-- Name: v_course_approval_rate; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_course_approval_rate AS
 SELECT c.id AS course_id,
    c.name AS course_name,
    count(e.id) AS total_students,
    count(
        CASE
            WHEN ((e.status)::text = 'APPROVED'::text) THEN 1
            ELSE NULL::integer
        END) AS approved_students,
    round((((count(
        CASE
            WHEN ((e.status)::text = 'APPROVED'::text) THEN 1
            ELSE NULL::integer
        END))::numeric / (NULLIF(count(e.id), 0))::numeric) * (100)::numeric), 2) AS approval_percentage
   FROM (public.course c
     LEFT JOIN public.enrollment e ON ((c.id = e.course_id)))
  WHERE (c.is_deleted = false)
  GROUP BY c.id, c.name;


--
-- Name: v_extraordinarios_estudiante; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_extraordinarios_estudiante AS
 SELECT s.enrollment_number,
    (((s.first_name)::text || ' '::text) || (s.last_name)::text) AS student_name,
    c.course_code,
    c.name AS course_name,
    ee.attempt_number,
    ee.scheduled_date,
    ee.previous_grade,
    ee.grade,
    ee.status,
    ee.payment_folio
   FROM ((public.extraordinary_exam ee
     JOIN public.student s ON ((ee.student_id = s.id)))
     JOIN public.course c ON ((ee.course_id = c.id)))
  WHERE (ee.is_deleted = false)
  ORDER BY s.enrollment_number, ee.attempt_number;


--
-- Name: v_kardex_oficial; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_kardex_oficial AS
 SELECT s.enrollment_number,
    (((s.first_name)::text || ' '::text) || (s.last_name)::text) AS full_name,
    c.course_code,
    c.name AS course_name,
    c.credits,
    ac.name AS academic_semester,
    ac.year,
    k.final_grade,
    k.letter_grade,
    k.status,
    k.attempt_number,
    k.enrollment_date,
    k.approval_date,
    k.official_folio,
    k.is_officialized,
    k.officialization_date,
    (((t.first_name)::text || ' '::text) || (t.last_name)::text) AS teacher_name
   FROM ((((((public.kardex k
     JOIN public.student s ON ((k.student_id = s.id)))
     JOIN public.course c ON ((k.course_id = c.id)))
     JOIN public.academic_semester ac ON ((k.academic_semester_id = ac.id)))
     LEFT JOIN public.enrollment e ON ((k.enrollment_id = e.id)))
     LEFT JOIN public.academic_group ag ON ((e.group_id = ag.id)))
     LEFT JOIN public.teacher t ON ((ag.teacher_id = t.id)))
  WHERE (k.is_deleted = false)
  ORDER BY s.enrollment_number, ac.year, ac.period;


--
-- Name: v_resumen_academico; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_resumen_academico AS
 SELECT s.id AS student_id,
    s.enrollment_number,
    (((s.first_name)::text || ' '::text) || (s.last_name)::text) AS full_name,
    g.name AS generation,
    count(k.id) AS total_subjects_enrolled,
    count(
        CASE
            WHEN ((k.status)::text = ANY ((ARRAY['APPROVED'::character varying, 'EXTRAORDINARY'::character varying])::text[])) THEN 1
            ELSE NULL::integer
        END) AS total_approved,
    count(
        CASE
            WHEN ((k.status)::text = 'FAILED'::text) THEN 1
            ELSE NULL::integer
        END) AS total_failed,
    COALESCE(sum(
        CASE
            WHEN ((k.status)::text = ANY ((ARRAY['APPROVED'::character varying, 'EXTRAORDINARY'::character varying])::text[])) THEN c.credits
            ELSE 0
        END), (0)::bigint) AS credits_approved,
    COALESCE(sum(c.credits), (0)::bigint) AS credits_enrolled,
    round(avg(
        CASE
            WHEN ((k.status)::text = ANY ((ARRAY['APPROVED'::character varying, 'EXTRAORDINARY'::character varying])::text[])) THEN k.final_grade
            ELSE NULL::numeric
        END), 2) AS overall_average,
    public.fn_grade_to_letter(round(avg(
        CASE
            WHEN ((k.status)::text = ANY ((ARRAY['APPROVED'::character varying, 'EXTRAORDINARY'::character varying])::text[])) THEN k.final_grade
            ELSE NULL::numeric
        END), 2)) AS average_letter
   FROM (((public.student s
     LEFT JOIN public.kardex k ON (((s.id = k.student_id) AND (k.is_deleted = false))))
     LEFT JOIN public.course c ON ((k.course_id = c.id)))
     LEFT JOIN public.generation g ON ((s.generation_id = g.id)))
  WHERE (s.is_deleted = false)
  GROUP BY s.id, s.enrollment_number, s.first_name, s.last_name, g.name;


--
-- Name: v_student_transcript; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_student_transcript AS
 SELECT s.id AS student_id,
    s.enrollment_number,
    (((s.first_name)::text || ' '::text) || (s.last_name)::text) AS full_name,
    c.course_code,
    c.name AS course_name,
    ap.name AS academic_period,
    c.credits,
    round(sum(((g.score * et.weight) / (100)::numeric)), 2) AS final_grade,
    e.status
   FROM (((((public.student s
     JOIN public.enrollment e ON ((s.id = e.student_id)))
     JOIN public.course c ON ((e.course_id = c.id)))
     JOIN public.academic_period ap ON ((e.academic_period_id = ap.id)))
     LEFT JOIN public.grade g ON ((e.id = g.enrollment_id)))
     LEFT JOIN public.evaluation_type et ON ((g.evaluation_type_id = et.id)))
  WHERE ((s.is_deleted = false) AND (e.is_deleted = false))
  GROUP BY s.id, s.enrollment_number, s.first_name, s.last_name, c.course_code, c.name, ap.name, c.credits, e.status;


--
-- Name: v_student_gpa; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_student_gpa AS
 SELECT student_id,
    full_name,
    round(avg(final_grade), 2) AS gpa
   FROM public.v_student_transcript
  WHERE (final_grade IS NOT NULL)
  GROUP BY student_id, full_name;


--
-- Name: v_teacher_performance; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_teacher_performance AS
 SELECT t.id AS teacher_id,
    t.employee_number,
    (((t.first_name)::text || ' '::text) || (t.last_name)::text) AS teacher_name,
    count(DISTINCT ag.course_id) AS courses_taught,
    count(DISTINCT e.id) AS total_enrollments
   FROM ((public.teacher t
     LEFT JOIN public.academic_group ag ON (((t.id = ag.teacher_id) AND (ag.is_deleted = false))))
     LEFT JOIN public.enrollment e ON (((ag.id = e.group_id) AND (e.is_deleted = false))))
  WHERE (t.is_deleted = false)
  GROUP BY t.id, t.employee_number, t.first_name, t.last_name;


--
-- Name: academic_group academic_group_name_academic_semester_id_course_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.academic_group
    ADD CONSTRAINT academic_group_name_academic_semester_id_course_id_key UNIQUE (name, academic_semester_id, course_id);


--
-- Name: academic_group academic_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.academic_group
    ADD CONSTRAINT academic_group_pkey PRIMARY KEY (id);


--
-- Name: academic_period academic_period_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.academic_period
    ADD CONSTRAINT academic_period_name_key UNIQUE (name);


--
-- Name: academic_period academic_period_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.academic_period
    ADD CONSTRAINT academic_period_pkey PRIMARY KEY (id);


--
-- Name: academic_semester academic_semester_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.academic_semester
    ADD CONSTRAINT academic_semester_pkey PRIMARY KEY (id);


--
-- Name: access_audit access_audit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.access_audit
    ADD CONSTRAINT access_audit_pkey PRIMARY KEY (id);


--
-- Name: app_user app_user_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_email_key UNIQUE (email);


--
-- Name: app_user app_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_pkey PRIMARY KEY (id);


--
-- Name: app_user app_user_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_username_key UNIQUE (username);


--
-- Name: attendance attendance_enrollment_id_attendance_date_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance
    ADD CONSTRAINT attendance_enrollment_id_attendance_date_key UNIQUE (enrollment_id, attendance_date);


--
-- Name: attendance_period attendance_period_enrollment_id_academic_semester_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance_period
    ADD CONSTRAINT attendance_period_enrollment_id_academic_semester_id_key UNIQUE (enrollment_id, academic_semester_id);


--
-- Name: attendance_period attendance_period_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance_period
    ADD CONSTRAINT attendance_period_pkey PRIMARY KEY (id);


--
-- Name: attendance attendance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance
    ADD CONSTRAINT attendance_pkey PRIMARY KEY (id);


--
-- Name: certificate certificate_official_folio_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.certificate
    ADD CONSTRAINT certificate_official_folio_key UNIQUE (official_folio);


--
-- Name: certificate certificate_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.certificate
    ADD CONSTRAINT certificate_pkey PRIMARY KEY (id);


--
-- Name: conduct conduct_enrollment_id_academic_semester_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conduct
    ADD CONSTRAINT conduct_enrollment_id_academic_semester_id_key UNIQUE (enrollment_id, academic_semester_id);


--
-- Name: conduct_incident conduct_incident_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conduct_incident
    ADD CONSTRAINT conduct_incident_pkey PRIMARY KEY (id);


--
-- Name: conduct conduct_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conduct
    ADD CONSTRAINT conduct_pkey PRIMARY KEY (id);


--
-- Name: course course_course_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.course
    ADD CONSTRAINT course_course_code_key UNIQUE (course_code);


--
-- Name: course course_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.course
    ADD CONSTRAINT course_pkey PRIMARY KEY (id);


--
-- Name: educational_resource educational_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.educational_resource
    ADD CONSTRAINT educational_resource_pkey PRIMARY KEY (id);


--
-- Name: email_verification email_verification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.email_verification
    ADD CONSTRAINT email_verification_pkey PRIMARY KEY (id);


--
-- Name: enrollment enrollment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enrollment
    ADD CONSTRAINT enrollment_pkey PRIMARY KEY (id);


--
-- Name: enrollment enrollment_student_id_course_id_academic_period_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enrollment
    ADD CONSTRAINT enrollment_student_id_course_id_academic_period_id_key UNIQUE (student_id, course_id, academic_period_id);


--
-- Name: evaluation_type evaluation_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evaluation_type
    ADD CONSTRAINT evaluation_type_pkey PRIMARY KEY (id);


--
-- Name: extraordinary_exam extraordinary_exam_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.extraordinary_exam
    ADD CONSTRAINT extraordinary_exam_pkey PRIMARY KEY (id);


--
-- Name: extraordinary_exam extraordinary_exam_student_id_course_id_attempt_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.extraordinary_exam
    ADD CONSTRAINT extraordinary_exam_student_id_course_id_attempt_number_key UNIQUE (student_id, course_id, attempt_number);


--
-- Name: generation generation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.generation
    ADD CONSTRAINT generation_pkey PRIMARY KEY (id);


--
-- Name: grade grade_enrollment_id_evaluation_type_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.grade
    ADD CONSTRAINT grade_enrollment_id_evaluation_type_id_key UNIQUE (enrollment_id, evaluation_type_id);


--
-- Name: grade grade_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.grade
    ADD CONSTRAINT grade_pkey PRIMARY KEY (id);


--
-- Name: guardian guardian_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.guardian
    ADD CONSTRAINT guardian_pkey PRIMARY KEY (id);


--
-- Name: kardex kardex_official_folio_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kardex
    ADD CONSTRAINT kardex_official_folio_key UNIQUE (official_folio);


--
-- Name: kardex kardex_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kardex
    ADD CONSTRAINT kardex_pkey PRIMARY KEY (id);


--
-- Name: kardex kardex_student_id_course_id_academic_semester_id_attempt_nu_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kardex
    ADD CONSTRAINT kardex_student_id_course_id_academic_semester_id_attempt_nu_key UNIQUE (student_id, course_id, academic_semester_id, attempt_number);


--
-- Name: password_recovery password_recovery_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_recovery
    ADD CONSTRAINT password_recovery_pkey PRIMARY KEY (id);


--
-- Name: password_recovery password_recovery_recovery_token_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_recovery
    ADD CONSTRAINT password_recovery_recovery_token_key UNIQUE (recovery_token);


--
-- Name: permission permission_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT permission_code_key UNIQUE (code);


--
-- Name: permission permission_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT permission_pkey PRIMARY KEY (id);


--
-- Name: registration_request registration_request_curp_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.registration_request
    ADD CONSTRAINT registration_request_curp_key UNIQUE (curp);


--
-- Name: registration_request registration_request_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.registration_request
    ADD CONSTRAINT registration_request_pkey PRIMARY KEY (id);


--
-- Name: report_card_detail report_card_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_card_detail
    ADD CONSTRAINT report_card_detail_pkey PRIMARY KEY (id);


--
-- Name: report_card report_card_folio_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_card
    ADD CONSTRAINT report_card_folio_key UNIQUE (folio);


--
-- Name: report_card report_card_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_card
    ADD CONSTRAINT report_card_pkey PRIMARY KEY (id);


--
-- Name: retake_exam retake_exam_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retake_exam
    ADD CONSTRAINT retake_exam_pkey PRIMARY KEY (id);


--
-- Name: retake_exam retake_exam_student_id_course_id_academic_semester_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retake_exam
    ADD CONSTRAINT retake_exam_student_id_course_id_academic_semester_id_key UNIQUE (student_id, course_id, academic_semester_id);


--
-- Name: role role_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_name_key UNIQUE (name);


--
-- Name: role_permission role_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permission
    ADD CONSTRAINT role_permission_pkey PRIMARY KEY (role_id, permission_id);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- Name: semester semester_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.semester
    ADD CONSTRAINT semester_pkey PRIMARY KEY (id);


--
-- Name: student student_curp_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_curp_key UNIQUE (curp);


--
-- Name: student_document student_document_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_document
    ADD CONSTRAINT student_document_pkey PRIMARY KEY (id);


--
-- Name: student student_enrollment_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_enrollment_number_key UNIQUE (enrollment_number);


--
-- Name: student student_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_pkey PRIMARY KEY (id);


--
-- Name: student student_user_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_user_id_key UNIQUE (user_id);


--
-- Name: study_plan study_plan_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.study_plan
    ADD CONSTRAINT study_plan_code_key UNIQUE (code);


--
-- Name: study_plan study_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.study_plan
    ADD CONSTRAINT study_plan_pkey PRIMARY KEY (id);


--
-- Name: system_configuration system_configuration_config_key_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_configuration
    ADD CONSTRAINT system_configuration_config_key_key UNIQUE (config_key);


--
-- Name: system_configuration system_configuration_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_configuration
    ADD CONSTRAINT system_configuration_pkey PRIMARY KEY (id);


--
-- Name: teacher teacher_curp_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_curp_key UNIQUE (curp);


--
-- Name: teacher teacher_employee_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_employee_number_key UNIQUE (employee_number);


--
-- Name: teacher teacher_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_pkey PRIMARY KEY (id);


--
-- Name: teacher teacher_rfc_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_rfc_key UNIQUE (rfc);


--
-- Name: teacher teacher_user_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_user_id_key UNIQUE (user_id);


--
-- Name: user_profile uk4r6quxdy0ok0t5jwdqrruce0v; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile
    ADD CONSTRAINT uk4r6quxdy0ok0t5jwdqrruce0v UNIQUE (institutional_email);


--
-- Name: attendance_period uk9myvtos5m5cqsti9ypft16x2v; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance_period
    ADD CONSTRAINT uk9myvtos5m5cqsti9ypft16x2v UNIQUE (enrollment_id, academic_semester_id);


--
-- Name: user_profile ukarseuafdbf88w1hjx1alumfm6; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile
    ADD CONSTRAINT ukarseuafdbf88w1hjx1alumfm6 UNIQUE (rfc);


--
-- Name: user_profile ukc8k4g4qbl3n8qur6p5wt60rsm; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile
    ADD CONSTRAINT ukc8k4g4qbl3n8qur6p5wt60rsm UNIQUE (enrollment_number);


--
-- Name: user_profile ukebc21hy5j7scdvcjt0jy6xxrv; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile
    ADD CONSTRAINT ukebc21hy5j7scdvcjt0jy6xxrv UNIQUE (user_id);


--
-- Name: user_profile ukh02uo5s22wiu7eh3kktovwjjh; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile
    ADD CONSTRAINT ukh02uo5s22wiu7eh3kktovwjjh UNIQUE (curp);


--
-- Name: user_profile ukihu4rswpa8fbdmucpoh17p5vj; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile
    ADD CONSTRAINT ukihu4rswpa8fbdmucpoh17p5vj UNIQUE (employee_number);


--
-- Name: user_profile user_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile
    ADD CONSTRAINT user_profile_pkey PRIMARY KEY (id);


--
-- Name: user_role user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role
    ADD CONSTRAINT user_role_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: user_session user_session_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_session
    ADD CONSTRAINT user_session_pkey PRIMARY KEY (id);


--
-- Name: idx_academic_group_course; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_academic_group_course ON public.academic_group USING btree (course_id);


--
-- Name: idx_academic_group_semester; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_academic_group_semester ON public.academic_group USING btree (academic_semester_id);


--
-- Name: idx_academic_group_teacher; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_academic_group_teacher ON public.academic_group USING btree (teacher_id);


--
-- Name: idx_academic_semester_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_academic_semester_status ON public.academic_semester USING btree (status);


--
-- Name: idx_academic_semester_year; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_academic_semester_year ON public.academic_semester USING btree (year);


--
-- Name: idx_access_audit_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_access_audit_created_at ON public.access_audit USING btree (created_at);


--
-- Name: idx_access_audit_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_access_audit_date ON public.access_audit USING btree (created_at);


--
-- Name: idx_access_audit_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_access_audit_user ON public.access_audit USING btree (user_id);


--
-- Name: idx_app_user_email; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_user_email ON public.app_user USING btree (email);


--
-- Name: idx_attendance_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attendance_date ON public.attendance USING btree (attendance_date);


--
-- Name: idx_attendance_enrollment; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attendance_enrollment ON public.attendance USING btree (enrollment_id);


--
-- Name: idx_certificate_student; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_certificate_student ON public.certificate USING btree (student_id);


--
-- Name: idx_course_semester; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_course_semester ON public.course USING btree (semester_id);


--
-- Name: idx_course_study_plan; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_course_study_plan ON public.course USING btree (study_plan_id);


--
-- Name: idx_educational_resource_course; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_educational_resource_course ON public.educational_resource USING btree (course_id);


--
-- Name: idx_email_verification_code; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_email_verification_code ON public.email_verification USING btree (verification_code);


--
-- Name: idx_email_verification_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_email_verification_user ON public.email_verification USING btree (user_id);


--
-- Name: idx_enrollment_course; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_enrollment_course ON public.enrollment USING btree (course_id);


--
-- Name: idx_enrollment_group; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_enrollment_group ON public.enrollment USING btree (group_id);


--
-- Name: idx_enrollment_period; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_enrollment_period ON public.enrollment USING btree (academic_period_id);


--
-- Name: idx_enrollment_student; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_enrollment_student ON public.enrollment USING btree (student_id);


--
-- Name: idx_evaluation_course; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_evaluation_course ON public.evaluation_type USING btree (course_id);


--
-- Name: idx_extraordinary_exam_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_extraordinary_exam_status ON public.extraordinary_exam USING btree (status);


--
-- Name: idx_extraordinary_exam_student; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_extraordinary_exam_student ON public.extraordinary_exam USING btree (student_id);


--
-- Name: idx_generation_entry_year; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_generation_entry_year ON public.generation USING btree (entry_year);


--
-- Name: idx_grade_enrollment; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_grade_enrollment ON public.grade USING btree (enrollment_id);


--
-- Name: idx_guardian_student; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_guardian_student ON public.guardian USING btree (student_id);


--
-- Name: idx_kardex_course; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_kardex_course ON public.kardex USING btree (course_id);


--
-- Name: idx_kardex_semester; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_kardex_semester ON public.kardex USING btree (academic_semester_id);


--
-- Name: idx_kardex_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_kardex_status ON public.kardex USING btree (status);


--
-- Name: idx_kardex_student; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_kardex_student ON public.kardex USING btree (student_id);


--
-- Name: idx_registration_request_curp; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_registration_request_curp ON public.registration_request USING btree (curp);


--
-- Name: idx_registration_request_email; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_registration_request_email ON public.registration_request USING btree (email);


--
-- Name: idx_registration_request_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_registration_request_status ON public.registration_request USING btree (status);


--
-- Name: idx_report_card_detail_report_card; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_report_card_detail_report_card ON public.report_card_detail USING btree (report_card_id);


--
-- Name: idx_report_card_folio; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_report_card_folio ON public.report_card USING btree (folio);


--
-- Name: idx_report_card_generation_mode; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_report_card_generation_mode ON public.report_card USING btree (generation_mode);


--
-- Name: idx_report_card_semester; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_report_card_semester ON public.report_card USING btree (academic_semester_id);


--
-- Name: idx_report_card_student; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_report_card_student ON public.report_card USING btree (student_id);


--
-- Name: idx_semester_study_plan; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_semester_study_plan ON public.semester USING btree (study_plan_id);


--
-- Name: idx_student_curp; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_curp ON public.student USING btree (curp);


--
-- Name: idx_student_document_student; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_document_student ON public.student_document USING btree (student_id);


--
-- Name: idx_student_enrollment; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_enrollment ON public.student USING btree (enrollment_number);


--
-- Name: idx_student_enrollment_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_enrollment_date ON public.student USING btree (enrollment_date);


--
-- Name: idx_student_gender; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_gender ON public.student USING btree (gender);


--
-- Name: idx_user_session_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_session_user ON public.user_session USING btree (user_id);


--
-- Name: academic_semester trg_only_one_current_semester; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_only_one_current_semester BEFORE INSERT OR UPDATE ON public.academic_semester FOR EACH ROW EXECUTE FUNCTION public.fn_only_one_current_semester();


--
-- Name: academic_group trg_update_academic_group; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_academic_group BEFORE UPDATE ON public.academic_group FOR EACH ROW EXECUTE FUNCTION public.fn_update_academic_group();


--
-- Name: academic_semester trg_update_academic_semester; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_academic_semester BEFORE UPDATE ON public.academic_semester FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: app_user trg_update_app_user; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_app_user BEFORE UPDATE ON public.app_user FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: certificate trg_update_certificate; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_certificate BEFORE UPDATE ON public.certificate FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: conduct trg_update_conduct; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_conduct BEFORE UPDATE ON public.conduct FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: course trg_update_course; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_course BEFORE UPDATE ON public.course FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: enrollment trg_update_enrollment; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_enrollment BEFORE UPDATE ON public.enrollment FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: generation trg_update_generation; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_generation BEFORE UPDATE ON public.generation FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: guardian trg_update_guardian; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_guardian BEFORE UPDATE ON public.guardian FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: kardex trg_update_kardex; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_kardex BEFORE UPDATE ON public.kardex FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: report_card trg_update_report_card; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_report_card BEFORE UPDATE ON public.report_card FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: role trg_update_role; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_role BEFORE UPDATE ON public.role FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: student trg_update_student; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_student BEFORE UPDATE ON public.student FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: student_document trg_update_student_document; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_student_document BEFORE UPDATE ON public.student_document FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: study_plan trg_update_study_plan; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_study_plan BEFORE UPDATE ON public.study_plan FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: system_configuration trg_update_system_configuration; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_system_configuration BEFORE UPDATE ON public.system_configuration FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: teacher trg_update_teacher; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_update_teacher BEFORE UPDATE ON public.teacher FOR EACH ROW EXECUTE FUNCTION public.fn_set_updated_at();


--
-- Name: evaluation_type trg_validate_course_weight; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_validate_course_weight AFTER INSERT OR UPDATE ON public.evaluation_type FOR EACH ROW EXECUTE FUNCTION public.fn_validate_course_weight();


--
-- Name: academic_group academic_group_academic_semester_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.academic_group
    ADD CONSTRAINT academic_group_academic_semester_id_fkey FOREIGN KEY (academic_semester_id) REFERENCES public.academic_semester(id) ON DELETE CASCADE;


--
-- Name: academic_group academic_group_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.academic_group
    ADD CONSTRAINT academic_group_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.course(id) ON DELETE CASCADE;


--
-- Name: academic_group academic_group_teacher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.academic_group
    ADD CONSTRAINT academic_group_teacher_id_fkey FOREIGN KEY (teacher_id) REFERENCES public.teacher(id) ON DELETE SET NULL;


--
-- Name: access_audit access_audit_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.access_audit
    ADD CONSTRAINT access_audit_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: attendance attendance_enrollment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance
    ADD CONSTRAINT attendance_enrollment_id_fkey FOREIGN KEY (enrollment_id) REFERENCES public.enrollment(id) ON DELETE CASCADE;


--
-- Name: attendance attendance_justified_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance
    ADD CONSTRAINT attendance_justified_by_fkey FOREIGN KEY (justified_by) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: attendance_period attendance_period_academic_semester_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance_period
    ADD CONSTRAINT attendance_period_academic_semester_id_fkey FOREIGN KEY (academic_semester_id) REFERENCES public.academic_semester(id) ON DELETE RESTRICT;


--
-- Name: attendance_period attendance_period_enrollment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance_period
    ADD CONSTRAINT attendance_period_enrollment_id_fkey FOREIGN KEY (enrollment_id) REFERENCES public.enrollment(id) ON DELETE CASCADE;


--
-- Name: attendance attendance_recorded_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance
    ADD CONSTRAINT attendance_recorded_by_fkey FOREIGN KEY (recorded_by) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: certificate certificate_director_signer_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.certificate
    ADD CONSTRAINT certificate_director_signer_fkey FOREIGN KEY (director_signer) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: certificate certificate_generation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.certificate
    ADD CONSTRAINT certificate_generation_id_fkey FOREIGN KEY (generation_id) REFERENCES public.generation(id) ON DELETE SET NULL;


--
-- Name: certificate certificate_secretary_signer_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.certificate
    ADD CONSTRAINT certificate_secretary_signer_fkey FOREIGN KEY (secretary_signer) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: certificate certificate_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.certificate
    ADD CONSTRAINT certificate_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE RESTRICT;


--
-- Name: conduct conduct_academic_semester_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conduct
    ADD CONSTRAINT conduct_academic_semester_id_fkey FOREIGN KEY (academic_semester_id) REFERENCES public.academic_semester(id) ON DELETE RESTRICT;


--
-- Name: conduct conduct_enrollment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conduct
    ADD CONSTRAINT conduct_enrollment_id_fkey FOREIGN KEY (enrollment_id) REFERENCES public.enrollment(id) ON DELETE RESTRICT;


--
-- Name: conduct_incident conduct_incident_enrollment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conduct_incident
    ADD CONSTRAINT conduct_incident_enrollment_id_fkey FOREIGN KEY (enrollment_id) REFERENCES public.enrollment(id) ON DELETE CASCADE;


--
-- Name: conduct_incident conduct_incident_recorded_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conduct_incident
    ADD CONSTRAINT conduct_incident_recorded_by_fkey FOREIGN KEY (recorded_by) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: conduct conduct_recorded_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conduct
    ADD CONSTRAINT conduct_recorded_by_fkey FOREIGN KEY (recorded_by) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: course course_semester_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.course
    ADD CONSTRAINT course_semester_id_fkey FOREIGN KEY (semester_id) REFERENCES public.semester(id) ON DELETE SET NULL;


--
-- Name: course course_study_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.course
    ADD CONSTRAINT course_study_plan_id_fkey FOREIGN KEY (study_plan_id) REFERENCES public.study_plan(id) ON DELETE SET NULL;


--
-- Name: educational_resource educational_resource_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.educational_resource
    ADD CONSTRAINT educational_resource_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.course(id) ON DELETE SET NULL;


--
-- Name: email_verification email_verification_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.email_verification
    ADD CONSTRAINT email_verification_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE CASCADE;


--
-- Name: enrollment enrollment_academic_period_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enrollment
    ADD CONSTRAINT enrollment_academic_period_id_fkey FOREIGN KEY (academic_period_id) REFERENCES public.academic_period(id) ON DELETE RESTRICT;


--
-- Name: enrollment enrollment_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enrollment
    ADD CONSTRAINT enrollment_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.course(id) ON DELETE RESTRICT;


--
-- Name: enrollment enrollment_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enrollment
    ADD CONSTRAINT enrollment_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.academic_group(id) ON DELETE SET NULL;


--
-- Name: enrollment enrollment_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enrollment
    ADD CONSTRAINT enrollment_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE RESTRICT;


--
-- Name: evaluation_type evaluation_type_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evaluation_type
    ADD CONSTRAINT evaluation_type_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.course(id) ON DELETE CASCADE;


--
-- Name: extraordinary_exam extraordinary_exam_academic_semester_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.extraordinary_exam
    ADD CONSTRAINT extraordinary_exam_academic_semester_id_fkey FOREIGN KEY (academic_semester_id) REFERENCES public.academic_semester(id) ON DELETE SET NULL;


--
-- Name: extraordinary_exam extraordinary_exam_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.extraordinary_exam
    ADD CONSTRAINT extraordinary_exam_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.course(id) ON DELETE RESTRICT;


--
-- Name: extraordinary_exam extraordinary_exam_examiner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.extraordinary_exam
    ADD CONSTRAINT extraordinary_exam_examiner_id_fkey FOREIGN KEY (examiner_id) REFERENCES public.teacher(id) ON DELETE SET NULL;


--
-- Name: extraordinary_exam extraordinary_exam_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.extraordinary_exam
    ADD CONSTRAINT extraordinary_exam_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE RESTRICT;


--
-- Name: user_profile fkpdmw33px6fmevqhcy2lpstu4w; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profile
    ADD CONSTRAINT fkpdmw33px6fmevqhcy2lpstu4w FOREIGN KEY (user_id) REFERENCES public.app_user(id);


--
-- Name: grade grade_enrollment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.grade
    ADD CONSTRAINT grade_enrollment_id_fkey FOREIGN KEY (enrollment_id) REFERENCES public.enrollment(id) ON DELETE CASCADE;


--
-- Name: grade grade_evaluation_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.grade
    ADD CONSTRAINT grade_evaluation_type_id_fkey FOREIGN KEY (evaluation_type_id) REFERENCES public.evaluation_type(id) ON DELETE CASCADE;


--
-- Name: grade grade_recorded_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.grade
    ADD CONSTRAINT grade_recorded_by_fkey FOREIGN KEY (recorded_by) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: guardian guardian_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.guardian
    ADD CONSTRAINT guardian_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE CASCADE;


--
-- Name: kardex kardex_academic_semester_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kardex
    ADD CONSTRAINT kardex_academic_semester_id_fkey FOREIGN KEY (academic_semester_id) REFERENCES public.academic_semester(id) ON DELETE RESTRICT;


--
-- Name: kardex kardex_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kardex
    ADD CONSTRAINT kardex_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.course(id) ON DELETE RESTRICT;


--
-- Name: kardex kardex_enrollment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kardex
    ADD CONSTRAINT kardex_enrollment_id_fkey FOREIGN KEY (enrollment_id) REFERENCES public.enrollment(id) ON DELETE SET NULL;


--
-- Name: kardex kardex_officialized_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kardex
    ADD CONSTRAINT kardex_officialized_by_fkey FOREIGN KEY (officialized_by) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: kardex kardex_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kardex
    ADD CONSTRAINT kardex_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE RESTRICT;


--
-- Name: password_recovery password_recovery_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_recovery
    ADD CONSTRAINT password_recovery_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE CASCADE;


--
-- Name: registration_request registration_request_processed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.registration_request
    ADD CONSTRAINT registration_request_processed_by_fkey FOREIGN KEY (processed_by) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: registration_request registration_request_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.registration_request
    ADD CONSTRAINT registration_request_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE SET NULL;


--
-- Name: registration_request registration_request_teacher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.registration_request
    ADD CONSTRAINT registration_request_teacher_id_fkey FOREIGN KEY (teacher_id) REFERENCES public.teacher(id) ON DELETE SET NULL;


--
-- Name: report_card report_card_academic_semester_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_card
    ADD CONSTRAINT report_card_academic_semester_id_fkey FOREIGN KEY (academic_semester_id) REFERENCES public.academic_semester(id) ON DELETE RESTRICT;


--
-- Name: report_card_detail report_card_detail_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_card_detail
    ADD CONSTRAINT report_card_detail_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.course(id) ON DELETE RESTRICT;


--
-- Name: report_card_detail report_card_detail_kardex_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_card_detail
    ADD CONSTRAINT report_card_detail_kardex_id_fkey FOREIGN KEY (kardex_id) REFERENCES public.kardex(id) ON DELETE SET NULL;


--
-- Name: report_card_detail report_card_detail_report_card_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_card_detail
    ADD CONSTRAINT report_card_detail_report_card_id_fkey FOREIGN KEY (report_card_id) REFERENCES public.report_card(id) ON DELETE CASCADE;


--
-- Name: report_card report_card_generation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_card
    ADD CONSTRAINT report_card_generation_id_fkey FOREIGN KEY (generation_id) REFERENCES public.generation(id) ON DELETE SET NULL;


--
-- Name: report_card report_card_signed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_card
    ADD CONSTRAINT report_card_signed_by_fkey FOREIGN KEY (signed_by) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: report_card report_card_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_card
    ADD CONSTRAINT report_card_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE RESTRICT;


--
-- Name: retake_exam retake_exam_academic_semester_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retake_exam
    ADD CONSTRAINT retake_exam_academic_semester_id_fkey FOREIGN KEY (academic_semester_id) REFERENCES public.academic_semester(id) ON DELETE RESTRICT;


--
-- Name: retake_exam retake_exam_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retake_exam
    ADD CONSTRAINT retake_exam_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.course(id) ON DELETE RESTRICT;


--
-- Name: retake_exam retake_exam_origin_semester_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retake_exam
    ADD CONSTRAINT retake_exam_origin_semester_id_fkey FOREIGN KEY (origin_semester_id) REFERENCES public.academic_semester(id);


--
-- Name: retake_exam retake_exam_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retake_exam
    ADD CONSTRAINT retake_exam_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE RESTRICT;


--
-- Name: role_permission role_permission_permission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permission
    ADD CONSTRAINT role_permission_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES public.permission(id) ON DELETE CASCADE;


--
-- Name: role_permission role_permission_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permission
    ADD CONSTRAINT role_permission_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.role(id) ON DELETE CASCADE;


--
-- Name: semester semester_study_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.semester
    ADD CONSTRAINT semester_study_plan_id_fkey FOREIGN KEY (study_plan_id) REFERENCES public.study_plan(id) ON DELETE CASCADE;


--
-- Name: student_document student_document_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_document
    ADD CONSTRAINT student_document_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE CASCADE;


--
-- Name: student_document student_document_verified_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_document
    ADD CONSTRAINT student_document_verified_by_fkey FOREIGN KEY (verified_by) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: student student_generation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_generation_id_fkey FOREIGN KEY (generation_id) REFERENCES public.generation(id) ON DELETE SET NULL;


--
-- Name: student student_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: teacher teacher_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE SET NULL;


--
-- Name: user_role user_role_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role
    ADD CONSTRAINT user_role_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.role(id) ON DELETE CASCADE;


--
-- Name: user_role user_role_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role
    ADD CONSTRAINT user_role_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE CASCADE;


--
-- Name: user_session user_session_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_session
    ADD CONSTRAINT user_session_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--


