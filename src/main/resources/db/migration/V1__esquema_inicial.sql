--
-- PostgreSQL database dump
--

\restrict 3zkyIwj1wPVgWzBPhmtLJounZqesiYpgCz5KeVNymYm1O13KELeOmpm1IGkV75U

-- Dumped from database version 18.4 (Homebrew)
-- Dumped by pg_dump version 18.4 (Homebrew)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;


--
-- Name: EXTENSION pg_trgm; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pg_trgm IS 'text similarity measurement and index searching based on trigrams';


--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- Name: appointment_status; Type: TYPE; Schema: public; Owner: rome
--

CREATE TYPE public.appointment_status AS ENUM (
    'pendiente',
    'confirmada',
    'en_proceso',
    'completada',
    'cancelada',
    'no_presentado'
);


ALTER TYPE public.appointment_status OWNER TO rome;

--
-- Name: client_frequency; Type: TYPE; Schema: public; Owner: rome
--

CREATE TYPE public.client_frequency AS ENUM (
    'alta',
    'media',
    'baja'
);


ALTER TYPE public.client_frequency OWNER TO rome;

--
-- Name: day_of_week; Type: TYPE; Schema: public; Owner: rome
--

CREATE TYPE public.day_of_week AS ENUM (
    'lunes',
    'martes',
    'miercoles',
    'jueves',
    'viernes',
    'sabado',
    'domingo'
);


ALTER TYPE public.day_of_week OWNER TO rome;

--
-- Name: face_shape; Type: TYPE; Schema: public; Owner: rome
--

CREATE TYPE public.face_shape AS ENUM (
    'ovalado',
    'redondo',
    'cuadrado',
    'corazon',
    'diamante',
    'rectangular'
);


ALTER TYPE public.face_shape OWNER TO rome;

--
-- Name: hair_type; Type: TYPE; Schema: public; Owner: rome
--

CREATE TYPE public.hair_type AS ENUM (
    'lacio',
    'ondulado',
    'rizado',
    'crespo',
    'afro'
);


ALTER TYPE public.hair_type OWNER TO rome;

--
-- Name: payment_method; Type: TYPE; Schema: public; Owner: rome
--

CREATE TYPE public.payment_method AS ENUM (
    'efectivo',
    'tarjeta_credito',
    'tarjeta_debito',
    'transferencia',
    'otro'
);


ALTER TYPE public.payment_method OWNER TO rome;

--
-- Name: payment_status; Type: TYPE; Schema: public; Owner: rome
--

CREATE TYPE public.payment_status AS ENUM (
    'pendiente',
    'pagado',
    'reembolsado',
    'fallido'
);


ALTER TYPE public.payment_status OWNER TO rome;

--
-- Name: service_category; Type: TYPE; Schema: public; Owner: rome
--

CREATE TYPE public.service_category AS ENUM (
    'cabello',
    'manos',
    'pies',
    'caballeros',
    'facial',
    'otro'
);


ALTER TYPE public.service_category OWNER TO rome;

--
-- Name: skin_tone; Type: TYPE; Schema: public; Owner: rome
--

CREATE TYPE public.skin_tone AS ENUM (
    'muy_clara',
    'clara',
    'morena_clara',
    'morena',
    'morena_oscura',
    'oscura'
);


ALTER TYPE public.skin_tone OWNER TO rome;

--
-- Name: user_role; Type: TYPE; Schema: public; Owner: rome
--

CREATE TYPE public.user_role AS ENUM (
    'administrador',
    'estilista',
    'recepcionista',
    'cliente'
);


ALTER TYPE public.user_role OWNER TO rome;

--
-- Name: set_updated_at(); Type: FUNCTION; Schema: public; Owner: rome
--

CREATE FUNCTION public.set_updated_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$;


ALTER FUNCTION public.set_updated_at() OWNER TO rome;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: appointment_services; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.appointment_services (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    appointment_id uuid NOT NULL,
    service_id uuid NOT NULL,
    price_at_time numeric(10,2) NOT NULL,
    duration_at_time integer NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.appointment_services OWNER TO rome;

--
-- Name: appointments; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.appointments (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    business_id uuid NOT NULL,
    client_id uuid NOT NULL,
    staff_id uuid,
    scheduled_at timestamp with time zone NOT NULL,
    ends_at timestamp with time zone NOT NULL,
    status public.appointment_status DEFAULT 'pendiente'::public.appointment_status NOT NULL,
    notes text,
    cancellation_reason text,
    cancelled_by uuid,
    cancelled_at timestamp with time zone,
    created_by uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    CONSTRAINT cancelled_fields_consistent CHECK ((((status = 'cancelada'::public.appointment_status) AND (cancelled_at IS NOT NULL)) OR (status <> 'cancelada'::public.appointment_status))),
    CONSTRAINT valid_appointment_window CHECK ((ends_at > scheduled_at))
);


ALTER TABLE public.appointments OWNER TO rome;

--
-- Name: audit_logs; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.audit_logs (
    id bigint NOT NULL,
    user_id uuid,
    action text NOT NULL,
    table_name text,
    record_id uuid,
    old_values jsonb,
    new_values jsonb,
    ip_address inet,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.audit_logs OWNER TO rome;

--
-- Name: audit_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: rome
--

CREATE SEQUENCE public.audit_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.audit_logs_id_seq OWNER TO rome;

--
-- Name: audit_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rome
--

ALTER SEQUENCE public.audit_logs_id_seq OWNED BY public.audit_logs.id;


--
-- Name: business_hours; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.business_hours (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    business_id uuid NOT NULL,
    day public.day_of_week NOT NULL,
    opens_at time without time zone,
    closes_at time without time zone,
    is_closed boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT valid_hours CHECK (((is_closed = true) OR ((opens_at IS NOT NULL) AND (closes_at IS NOT NULL) AND (opens_at < closes_at))))
);


ALTER TABLE public.business_hours OWNER TO rome;

--
-- Name: businesses; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.businesses (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    name text NOT NULL,
    address text,
    city text,
    country character(2) DEFAULT 'MX'::bpchar NOT NULL,
    phone text,
    email text,
    website text,
    logo_url text,
    currency character(3) DEFAULT 'USD'::bpchar NOT NULL,
    timezone text DEFAULT 'America/Mexico_City'::text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.businesses OWNER TO rome;

--
-- Name: client_notes; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.client_notes (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    client_id uuid NOT NULL,
    staff_id uuid,
    content text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


ALTER TABLE public.client_notes OWNER TO rome;

--
-- Name: client_preferences; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.client_preferences (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    client_id uuid NOT NULL,
    key text NOT NULL,
    value text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.client_preferences OWNER TO rome;

--
-- Name: clients; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.clients (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid,
    business_id uuid NOT NULL,
    name text NOT NULL,
    email text,
    phone text,
    birth_date date,
    address text,
    frequency public.client_frequency DEFAULT 'baja'::public.client_frequency NOT NULL,
    total_visits integer DEFAULT 0 NOT NULL,
    total_spent numeric(10,2) DEFAULT 0 NOT NULL,
    last_visit_at date,
    notes text,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    CONSTRAINT clients_email_format CHECK (((email IS NULL) OR (email ~* '^[^@]+@[^@]+\.[^@]+$'::text))),
    CONSTRAINT clients_total_spent_check CHECK ((total_spent >= (0)::numeric)),
    CONSTRAINT clients_total_visits_check CHECK ((total_visits >= 0))
);


ALTER TABLE public.clients OWNER TO rome;

--
-- Name: facial_analyses; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.facial_analyses (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    client_id uuid NOT NULL,
    staff_id uuid,
    image_url text,
    skin_tone public.skin_tone,
    skin_tone_hex character(7),
    hair_type public.hair_type,
    face_shape public.face_shape,
    confidence_pct numeric(5,2),
    raw_result jsonb,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT facial_analyses_confidence_pct_check CHECK (((confidence_pct >= (0)::numeric) AND (confidence_pct <= (100)::numeric)))
);


ALTER TABLE public.facial_analyses OWNER TO rome;

--
-- Name: facial_recommendations; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.facial_recommendations (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    analysis_id uuid NOT NULL,
    category text NOT NULL,
    title text NOT NULL,
    description text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.facial_recommendations OWNER TO rome;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO rome;

--
-- Name: notification_preferences; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.notification_preferences (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    appointment_reminders boolean DEFAULT true NOT NULL,
    new_clients boolean DEFAULT true NOT NULL,
    cancellations boolean DEFAULT true NOT NULL,
    monthly_reports boolean DEFAULT false NOT NULL,
    system_updates boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.notification_preferences OWNER TO rome;

--
-- Name: notifications; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.notifications (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    type text NOT NULL,
    title text NOT NULL,
    body text,
    is_read boolean DEFAULT false NOT NULL,
    read_at timestamp with time zone,
    metadata jsonb,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.notifications OWNER TO rome;

--
-- Name: payments; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.payments (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    appointment_id uuid NOT NULL,
    amount numeric(10,2) NOT NULL,
    method public.payment_method NOT NULL,
    status public.payment_status DEFAULT 'pendiente'::public.payment_status NOT NULL,
    reference text,
    paid_at timestamp with time zone,
    notes text,
    created_by uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT payments_amount_check CHECK ((amount > (0)::numeric))
);


ALTER TABLE public.payments OWNER TO rome;

--
-- Name: reviews; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.reviews (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    appointment_id uuid NOT NULL,
    client_id uuid NOT NULL,
    staff_id uuid,
    rating smallint NOT NULL,
    comment text,
    is_public boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT reviews_rating_check CHECK (((rating >= 1) AND (rating <= 5)))
);


ALTER TABLE public.reviews OWNER TO rome;

--
-- Name: services; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.services (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    business_id uuid NOT NULL,
    name text NOT NULL,
    description text,
    category public.service_category NOT NULL,
    duration_min integer NOT NULL,
    price numeric(10,2) NOT NULL,
    is_popular boolean DEFAULT false NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    display_order integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    CONSTRAINT services_duration_min_check CHECK ((duration_min > 0)),
    CONSTRAINT services_price_check CHECK ((price >= (0)::numeric))
);


ALTER TABLE public.services OWNER TO rome;

--
-- Name: staff; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.staff (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    business_id uuid NOT NULL,
    specialty text,
    bio text,
    hire_date date,
    commission_pct numeric(5,2) DEFAULT 0 NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT staff_commission_pct_check CHECK (((commission_pct >= (0)::numeric) AND (commission_pct <= (100)::numeric)))
);


ALTER TABLE public.staff OWNER TO rome;

--
-- Name: staff_schedules; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.staff_schedules (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    staff_id uuid NOT NULL,
    day public.day_of_week NOT NULL,
    starts_at time without time zone NOT NULL,
    ends_at time without time zone NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT valid_schedule CHECK ((starts_at < ends_at))
);


ALTER TABLE public.staff_schedules OWNER TO rome;

--
-- Name: staff_services; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.staff_services (
    staff_id uuid NOT NULL,
    service_id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.staff_services OWNER TO rome;

--
-- Name: user_sessions; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.user_sessions (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    refresh_token text NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    ip_address inet,
    user_agent text,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.user_sessions OWNER TO rome;

--
-- Name: users; Type: TABLE; Schema: public; Owner: rome
--

CREATE TABLE public.users (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    email text NOT NULL,
    password_hash text NOT NULL,
    name text NOT NULL,
    phone text,
    avatar_url text,
    role public.user_role DEFAULT 'cliente'::public.user_role NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    email_verified_at timestamp with time zone,
    last_login_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    CONSTRAINT users_email_format CHECK ((email ~* '^[^@]+@[^@]+\.[^@]+$'::text)),
    CONSTRAINT users_phone_format CHECK (((phone IS NULL) OR (phone ~ '^\+?[0-9\s\-\(\)]{7,20}$'::text)))
);


ALTER TABLE public.users OWNER TO rome;

--
-- Name: v_appointments_full; Type: VIEW; Schema: public; Owner: rome
--

CREATE VIEW public.v_appointments_full AS
SELECT
    NULL::uuid AS id,
    NULL::timestamp with time zone AS scheduled_at,
    NULL::timestamp with time zone AS ends_at,
    NULL::public.appointment_status AS status,
    NULL::text AS notes,
    NULL::timestamp with time zone AS created_at,
    NULL::uuid AS client_id,
    NULL::text AS client_name,
    NULL::text AS client_email,
    NULL::text AS client_phone,
    NULL::uuid AS staff_id,
    NULL::text AS staff_name,
    NULL::text AS staff_specialty,
    NULL::jsonb AS services,
    NULL::numeric AS total_price,
    NULL::bigint AS total_duration_min;


ALTER VIEW public.v_appointments_full OWNER TO rome;

--
-- Name: v_clients_summary; Type: VIEW; Schema: public; Owner: rome
--

CREATE VIEW public.v_clients_summary AS
SELECT
    NULL::uuid AS id,
    NULL::text AS name,
    NULL::text AS email,
    NULL::text AS phone,
    NULL::public.client_frequency AS frequency,
    NULL::integer AS total_visits,
    NULL::numeric(10,2) AS total_spent,
    NULL::date AS last_visit_at,
    NULL::timestamp with time zone AS created_at,
    NULL::timestamp with time zone AS next_appointment_at;


ALTER VIEW public.v_clients_summary OWNER TO rome;

--
-- Name: v_staff_performance; Type: VIEW; Schema: public; Owner: rome
--

CREATE VIEW public.v_staff_performance AS
 SELECT s.id AS staff_id,
    u.name AS staff_name,
    s.specialty,
    count(a.id) FILTER (WHERE (a.status = 'completada'::public.appointment_status)) AS total_appointments,
    COALESCE(sum(t.total), (0)::numeric) AS total_revenue,
    round(avg(r.rating), 2) AS avg_rating,
    count(r.id) AS total_reviews
   FROM ((((public.staff s
     JOIN public.users u ON ((u.id = s.user_id)))
     LEFT JOIN public.appointments a ON (((a.staff_id = s.id) AND (a.deleted_at IS NULL))))
     LEFT JOIN ( SELECT appointment_services.appointment_id,
            sum(appointment_services.price_at_time) AS total
           FROM public.appointment_services
          GROUP BY appointment_services.appointment_id) t ON ((t.appointment_id = a.id)))
     LEFT JOIN public.reviews r ON ((r.staff_id = s.id)))
  WHERE (s.is_active = true)
  GROUP BY s.id, u.name, s.specialty;


ALTER VIEW public.v_staff_performance OWNER TO rome;

--
-- Name: audit_logs id; Type: DEFAULT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.audit_logs ALTER COLUMN id SET DEFAULT nextval('public.audit_logs_id_seq'::regclass);


--
-- Name: appointment_services appointment_services_appointment_id_service_id_key; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.appointment_services
    ADD CONSTRAINT appointment_services_appointment_id_service_id_key UNIQUE (appointment_id, service_id);


--
-- Name: appointment_services appointment_services_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.appointment_services
    ADD CONSTRAINT appointment_services_pkey PRIMARY KEY (id);


--
-- Name: appointments appointments_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT appointments_pkey PRIMARY KEY (id);


--
-- Name: audit_logs audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (id);


--
-- Name: business_hours business_hours_business_id_day_key; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.business_hours
    ADD CONSTRAINT business_hours_business_id_day_key UNIQUE (business_id, day);


--
-- Name: business_hours business_hours_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.business_hours
    ADD CONSTRAINT business_hours_pkey PRIMARY KEY (id);


--
-- Name: businesses businesses_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.businesses
    ADD CONSTRAINT businesses_pkey PRIMARY KEY (id);


--
-- Name: client_notes client_notes_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.client_notes
    ADD CONSTRAINT client_notes_pkey PRIMARY KEY (id);


--
-- Name: client_preferences client_preferences_client_id_key_key; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.client_preferences
    ADD CONSTRAINT client_preferences_client_id_key_key UNIQUE (client_id, key);


--
-- Name: client_preferences client_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.client_preferences
    ADD CONSTRAINT client_preferences_pkey PRIMARY KEY (id);


--
-- Name: clients clients_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT clients_pkey PRIMARY KEY (id);


--
-- Name: clients clients_user_id_key; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT clients_user_id_key UNIQUE (user_id);


--
-- Name: facial_analyses facial_analyses_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.facial_analyses
    ADD CONSTRAINT facial_analyses_pkey PRIMARY KEY (id);


--
-- Name: facial_recommendations facial_recommendations_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.facial_recommendations
    ADD CONSTRAINT facial_recommendations_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: notification_preferences notification_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.notification_preferences
    ADD CONSTRAINT notification_preferences_pkey PRIMARY KEY (id);


--
-- Name: notification_preferences notification_preferences_user_id_key; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.notification_preferences
    ADD CONSTRAINT notification_preferences_user_id_key UNIQUE (user_id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- Name: reviews reviews_appointment_id_key; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_appointment_id_key UNIQUE (appointment_id);


--
-- Name: reviews reviews_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_pkey PRIMARY KEY (id);


--
-- Name: services services_business_id_name_key; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.services
    ADD CONSTRAINT services_business_id_name_key UNIQUE (business_id, name);


--
-- Name: services services_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.services
    ADD CONSTRAINT services_pkey PRIMARY KEY (id);


--
-- Name: staff staff_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.staff
    ADD CONSTRAINT staff_pkey PRIMARY KEY (id);


--
-- Name: staff_schedules staff_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.staff_schedules
    ADD CONSTRAINT staff_schedules_pkey PRIMARY KEY (id);


--
-- Name: staff_schedules staff_schedules_staff_id_day_key; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.staff_schedules
    ADD CONSTRAINT staff_schedules_staff_id_day_key UNIQUE (staff_id, day);


--
-- Name: staff_services staff_services_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.staff_services
    ADD CONSTRAINT staff_services_pkey PRIMARY KEY (staff_id, service_id);


--
-- Name: staff staff_user_id_key; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.staff
    ADD CONSTRAINT staff_user_id_key UNIQUE (user_id);


--
-- Name: user_sessions user_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_pkey PRIMARY KEY (id);


--
-- Name: user_sessions user_sessions_refresh_token_key; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_refresh_token_key UNIQUE (refresh_token);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_appointments_availability; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_appointments_availability ON public.appointments USING btree (staff_id, scheduled_at, ends_at) WHERE ((deleted_at IS NULL) AND (status <> ALL (ARRAY['cancelada'::public.appointment_status, 'no_presentado'::public.appointment_status])));


--
-- Name: idx_appointments_business_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_appointments_business_id ON public.appointments USING btree (business_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_appointments_client_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_appointments_client_id ON public.appointments USING btree (client_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_appointments_scheduled_at; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_appointments_scheduled_at ON public.appointments USING btree (scheduled_at) WHERE (deleted_at IS NULL);


--
-- Name: idx_appointments_staff_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_appointments_staff_id ON public.appointments USING btree (staff_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_appointments_status; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_appointments_status ON public.appointments USING btree (status) WHERE (deleted_at IS NULL);


--
-- Name: idx_appt_services_appointment_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_appt_services_appointment_id ON public.appointment_services USING btree (appointment_id);


--
-- Name: idx_appt_services_service_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_appt_services_service_id ON public.appointment_services USING btree (service_id);


--
-- Name: idx_audit_logs_created_at; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_audit_logs_created_at ON public.audit_logs USING btree (created_at DESC);


--
-- Name: idx_audit_logs_table; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_audit_logs_table ON public.audit_logs USING btree (table_name, record_id);


--
-- Name: idx_audit_logs_user_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_audit_logs_user_id ON public.audit_logs USING btree (user_id);


--
-- Name: idx_client_notes_client_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_client_notes_client_id ON public.client_notes USING btree (client_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_clients_business_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_clients_business_id ON public.clients USING btree (business_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_clients_email; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_clients_email ON public.clients USING btree (email) WHERE ((deleted_at IS NULL) AND (email IS NOT NULL));


--
-- Name: idx_clients_frequency; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_clients_frequency ON public.clients USING btree (frequency);


--
-- Name: idx_clients_last_visit; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_clients_last_visit ON public.clients USING btree (last_visit_at DESC);


--
-- Name: idx_clients_name_trgm; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_clients_name_trgm ON public.clients USING gin (name public.gin_trgm_ops);


--
-- Name: idx_facial_analyses_client_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_facial_analyses_client_id ON public.facial_analyses USING btree (client_id);


--
-- Name: idx_facial_analyses_created_at; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_facial_analyses_created_at ON public.facial_analyses USING btree (created_at DESC);


--
-- Name: idx_facial_recs_analysis_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_facial_recs_analysis_id ON public.facial_recommendations USING btree (analysis_id);


--
-- Name: idx_notifications_created; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_notifications_created ON public.notifications USING btree (created_at DESC);


--
-- Name: idx_notifications_user_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_notifications_user_id ON public.notifications USING btree (user_id, is_read);


--
-- Name: idx_payments_appointment_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_payments_appointment_id ON public.payments USING btree (appointment_id);


--
-- Name: idx_payments_paid_at; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_payments_paid_at ON public.payments USING btree (paid_at DESC);


--
-- Name: idx_payments_status; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_payments_status ON public.payments USING btree (status);


--
-- Name: idx_reviews_client_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_reviews_client_id ON public.reviews USING btree (client_id);


--
-- Name: idx_reviews_rating; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_reviews_rating ON public.reviews USING btree (rating);


--
-- Name: idx_reviews_staff_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_reviews_staff_id ON public.reviews USING btree (staff_id);


--
-- Name: idx_services_business_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_services_business_id ON public.services USING btree (business_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_services_category; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_services_category ON public.services USING btree (category) WHERE (deleted_at IS NULL);


--
-- Name: idx_services_is_popular; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_services_is_popular ON public.services USING btree (is_popular) WHERE (deleted_at IS NULL);


--
-- Name: idx_staff_business_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_staff_business_id ON public.staff USING btree (business_id) WHERE (is_active = true);


--
-- Name: idx_staff_user_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_staff_user_id ON public.staff USING btree (user_id);


--
-- Name: idx_user_sessions_expires; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_user_sessions_expires ON public.user_sessions USING btree (expires_at);


--
-- Name: idx_user_sessions_token; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_user_sessions_token ON public.user_sessions USING btree (refresh_token);


--
-- Name: idx_user_sessions_user_id; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_user_sessions_user_id ON public.user_sessions USING btree (user_id);


--
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_users_email ON public.users USING btree (email) WHERE (deleted_at IS NULL);


--
-- Name: idx_users_is_active; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_users_is_active ON public.users USING btree (is_active) WHERE (deleted_at IS NULL);


--
-- Name: idx_users_role; Type: INDEX; Schema: public; Owner: rome
--

CREATE INDEX idx_users_role ON public.users USING btree (role) WHERE (deleted_at IS NULL);


--
-- Name: v_appointments_full _RETURN; Type: RULE; Schema: public; Owner: rome
--

CREATE OR REPLACE VIEW public.v_appointments_full AS
 SELECT a.id,
    a.scheduled_at,
    a.ends_at,
    a.status,
    a.notes,
    a.created_at,
    c.id AS client_id,
    c.name AS client_name,
    c.email AS client_email,
    c.phone AS client_phone,
    s.id AS staff_id,
    u.name AS staff_name,
    s.specialty AS staff_specialty,
    COALESCE(jsonb_agg(jsonb_build_object('id', svc.id, 'name', svc.name, 'category', svc.category, 'price', aps.price_at_time, 'duration', aps.duration_at_time)) FILTER (WHERE (svc.id IS NOT NULL)), '[]'::jsonb) AS services,
    COALESCE(sum(aps.price_at_time), (0)::numeric) AS total_price,
    COALESCE(sum(aps.duration_at_time), (0)::bigint) AS total_duration_min
   FROM (((((public.appointments a
     JOIN public.clients c ON ((c.id = a.client_id)))
     LEFT JOIN public.staff s ON ((s.id = a.staff_id)))
     LEFT JOIN public.users u ON ((u.id = s.user_id)))
     LEFT JOIN public.appointment_services aps ON ((aps.appointment_id = a.id)))
     LEFT JOIN public.services svc ON ((svc.id = aps.service_id)))
  WHERE (a.deleted_at IS NULL)
  GROUP BY a.id, c.id, s.id, u.name;


--
-- Name: v_clients_summary _RETURN; Type: RULE; Schema: public; Owner: rome
--

CREATE OR REPLACE VIEW public.v_clients_summary AS
 SELECT c.id,
    c.name,
    c.email,
    c.phone,
    c.frequency,
    c.total_visits,
    c.total_spent,
    c.last_visit_at,
    c.created_at,
    min(a.scheduled_at) FILTER (WHERE ((a.scheduled_at > now()) AND (a.status = ANY (ARRAY['pendiente'::public.appointment_status, 'confirmada'::public.appointment_status])))) AS next_appointment_at
   FROM (public.clients c
     LEFT JOIN public.appointments a ON (((a.client_id = c.id) AND (a.deleted_at IS NULL))))
  WHERE (c.deleted_at IS NULL)
  GROUP BY c.id;


--
-- Name: appointments trg_appointments_updated_at; Type: TRIGGER; Schema: public; Owner: rome
--

CREATE TRIGGER trg_appointments_updated_at BEFORE UPDATE ON public.appointments FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: business_hours trg_business_hours_updated_at; Type: TRIGGER; Schema: public; Owner: rome
--

CREATE TRIGGER trg_business_hours_updated_at BEFORE UPDATE ON public.business_hours FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: businesses trg_businesses_updated_at; Type: TRIGGER; Schema: public; Owner: rome
--

CREATE TRIGGER trg_businesses_updated_at BEFORE UPDATE ON public.businesses FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: client_notes trg_client_notes_updated_at; Type: TRIGGER; Schema: public; Owner: rome
--

CREATE TRIGGER trg_client_notes_updated_at BEFORE UPDATE ON public.client_notes FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: client_preferences trg_client_preferences_updated_at; Type: TRIGGER; Schema: public; Owner: rome
--

CREATE TRIGGER trg_client_preferences_updated_at BEFORE UPDATE ON public.client_preferences FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: clients trg_clients_updated_at; Type: TRIGGER; Schema: public; Owner: rome
--

CREATE TRIGGER trg_clients_updated_at BEFORE UPDATE ON public.clients FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: notification_preferences trg_notification_prefs_updated_at; Type: TRIGGER; Schema: public; Owner: rome
--

CREATE TRIGGER trg_notification_prefs_updated_at BEFORE UPDATE ON public.notification_preferences FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: payments trg_payments_updated_at; Type: TRIGGER; Schema: public; Owner: rome
--

CREATE TRIGGER trg_payments_updated_at BEFORE UPDATE ON public.payments FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: reviews trg_reviews_updated_at; Type: TRIGGER; Schema: public; Owner: rome
--

CREATE TRIGGER trg_reviews_updated_at BEFORE UPDATE ON public.reviews FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: services trg_services_updated_at; Type: TRIGGER; Schema: public; Owner: rome
--

CREATE TRIGGER trg_services_updated_at BEFORE UPDATE ON public.services FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: staff_schedules trg_staff_schedules_updated_at; Type: TRIGGER; Schema: public; Owner: rome
--

CREATE TRIGGER trg_staff_schedules_updated_at BEFORE UPDATE ON public.staff_schedules FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: staff trg_staff_updated_at; Type: TRIGGER; Schema: public; Owner: rome
--

CREATE TRIGGER trg_staff_updated_at BEFORE UPDATE ON public.staff FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: users trg_users_updated_at; Type: TRIGGER; Schema: public; Owner: rome
--

CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();


--
-- Name: appointment_services appointment_services_appointment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.appointment_services
    ADD CONSTRAINT appointment_services_appointment_id_fkey FOREIGN KEY (appointment_id) REFERENCES public.appointments(id) ON DELETE CASCADE;


--
-- Name: appointment_services appointment_services_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.appointment_services
    ADD CONSTRAINT appointment_services_service_id_fkey FOREIGN KEY (service_id) REFERENCES public.services(id) ON DELETE RESTRICT;


--
-- Name: appointments appointments_business_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT appointments_business_id_fkey FOREIGN KEY (business_id) REFERENCES public.businesses(id) ON DELETE CASCADE;


--
-- Name: appointments appointments_cancelled_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT appointments_cancelled_by_fkey FOREIGN KEY (cancelled_by) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: appointments appointments_client_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT appointments_client_id_fkey FOREIGN KEY (client_id) REFERENCES public.clients(id) ON DELETE RESTRICT;


--
-- Name: appointments appointments_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT appointments_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: appointments appointments_staff_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT appointments_staff_id_fkey FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE SET NULL;


--
-- Name: audit_logs audit_logs_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: business_hours business_hours_business_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.business_hours
    ADD CONSTRAINT business_hours_business_id_fkey FOREIGN KEY (business_id) REFERENCES public.businesses(id) ON DELETE CASCADE;


--
-- Name: client_notes client_notes_client_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.client_notes
    ADD CONSTRAINT client_notes_client_id_fkey FOREIGN KEY (client_id) REFERENCES public.clients(id) ON DELETE CASCADE;


--
-- Name: client_notes client_notes_staff_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.client_notes
    ADD CONSTRAINT client_notes_staff_id_fkey FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE SET NULL;


--
-- Name: client_preferences client_preferences_client_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.client_preferences
    ADD CONSTRAINT client_preferences_client_id_fkey FOREIGN KEY (client_id) REFERENCES public.clients(id) ON DELETE CASCADE;


--
-- Name: clients clients_business_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT clients_business_id_fkey FOREIGN KEY (business_id) REFERENCES public.businesses(id) ON DELETE CASCADE;


--
-- Name: clients clients_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT clients_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: facial_analyses facial_analyses_client_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.facial_analyses
    ADD CONSTRAINT facial_analyses_client_id_fkey FOREIGN KEY (client_id) REFERENCES public.clients(id) ON DELETE CASCADE;


--
-- Name: facial_analyses facial_analyses_staff_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.facial_analyses
    ADD CONSTRAINT facial_analyses_staff_id_fkey FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE SET NULL;


--
-- Name: facial_recommendations facial_recommendations_analysis_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.facial_recommendations
    ADD CONSTRAINT facial_recommendations_analysis_id_fkey FOREIGN KEY (analysis_id) REFERENCES public.facial_analyses(id) ON DELETE CASCADE;


--
-- Name: notification_preferences notification_preferences_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.notification_preferences
    ADD CONSTRAINT notification_preferences_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: notifications notifications_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: payments payments_appointment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_appointment_id_fkey FOREIGN KEY (appointment_id) REFERENCES public.appointments(id) ON DELETE RESTRICT;


--
-- Name: payments payments_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: reviews reviews_appointment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_appointment_id_fkey FOREIGN KEY (appointment_id) REFERENCES public.appointments(id) ON DELETE CASCADE;


--
-- Name: reviews reviews_client_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_client_id_fkey FOREIGN KEY (client_id) REFERENCES public.clients(id) ON DELETE CASCADE;


--
-- Name: reviews reviews_staff_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_staff_id_fkey FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE SET NULL;


--
-- Name: services services_business_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.services
    ADD CONSTRAINT services_business_id_fkey FOREIGN KEY (business_id) REFERENCES public.businesses(id) ON DELETE CASCADE;


--
-- Name: staff staff_business_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.staff
    ADD CONSTRAINT staff_business_id_fkey FOREIGN KEY (business_id) REFERENCES public.businesses(id) ON DELETE CASCADE;


--
-- Name: staff_schedules staff_schedules_staff_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.staff_schedules
    ADD CONSTRAINT staff_schedules_staff_id_fkey FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE CASCADE;


--
-- Name: staff_services staff_services_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.staff_services
    ADD CONSTRAINT staff_services_service_id_fkey FOREIGN KEY (service_id) REFERENCES public.services(id) ON DELETE CASCADE;


--
-- Name: staff_services staff_services_staff_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.staff_services
    ADD CONSTRAINT staff_services_staff_id_fkey FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE CASCADE;


--
-- Name: staff staff_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.staff
    ADD CONSTRAINT staff_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: user_sessions user_sessions_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rome
--

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict 3zkyIwj1wPVgWzBPhmtLJounZqesiYpgCz5KeVNymYm1O13KELeOmpm1IGkV75U

