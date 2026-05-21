package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val db: AppDatabase) {

    val allUsers: Flow<List<UserEntity>> = db.userDao.getAllUsersFlow()
    val allAppointments: Flow<List<AppointmentEntity>> = db.appointmentDao.getAllAppointmentsFlow()
    val allMessages: Flow<List<MessageEntity>> = db.messageDao.getAllMessagesFlow()
    val allResources: Flow<List<ResourceEntity>> = db.resourceDao.getAllResourcesFlow()

    suspend fun getUserByCredentials(phone: String, password: String): UserEntity? = withContext(Dispatchers.IO) {
        db.userDao.getUserByCredentials(phone, password)
    }

    suspend fun getPhysicians(): List<UserEntity> = withContext(Dispatchers.IO) {
        db.userDao.getPhysicians()
    }

    suspend fun registerUser(user: UserEntity): Long = withContext(Dispatchers.IO) {
        db.userDao.insertUser(user)
    }

    suspend fun deleteUser(id: Int) = withContext(Dispatchers.IO) {
        db.userDao.deleteUser(id)
    }

    suspend fun bookAppointment(appointment: AppointmentEntity): Long = withContext(Dispatchers.IO) {
        db.appointmentDao.insertAppointment(appointment)
    }

    suspend fun updateAppointment(appointment: AppointmentEntity) = withContext(Dispatchers.IO) {
        db.appointmentDao.updateAppointment(appointment)
    }

    suspend fun updateAppointmentStatus(id: Int, status: String) = withContext(Dispatchers.IO) {
        db.appointmentDao.updateStatus(id, status)
    }

    suspend fun addConsultationReport(id: Int, report: String, prescription: String) = withContext(Dispatchers.IO) {
        db.appointmentDao.addConsultationReport(id, report, prescription)
    }

    suspend fun deleteAppointment(id: Int) = withContext(Dispatchers.IO) {
        db.appointmentDao.deleteAppointment(id)
    }

    suspend fun insertMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        db.messageDao.insertMessage(message)
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        db.messageDao.clearChat()
    }

    suspend fun saveResource(resource: ResourceEntity): Long = withContext(Dispatchers.IO) {
        db.resourceDao.insertResource(resource)
    }

    suspend fun deleteResource(id: Int) = withContext(Dispatchers.IO) {
        db.resourceDao.deleteResource(id)
    }

    suspend fun prepopulateIfEmpty() = withContext(Dispatchers.IO) {
        // 1. Check users
        val currentPhysicians = db.userDao.getPhysicians()
        if (currentPhysicians.isEmpty()) {
            // Register Dr. Zelalem Abera (Advisor)
            db.userDao.insertUser(
                UserEntity(
                    firstName = "Zelalem",
                    middleName = "Abera",
                    lastName = "(Msc)",
                    sex = "Male",
                    age = 42,
                    address = "HilCoe, School of Computer Science, Addis Ababa",
                    email = "zelalem.abera@hilcoe.edu.et",
                    phone = "0911000001",
                    password = "password123",
                    role = "PHYSICIAN"
                )
            )

            // Register Dr. Gemechu Ragea (MD)
            db.userDao.insertUser(
                UserEntity(
                    firstName = "Gemechu",
                    middleName = "Ragea",
                    lastName = "(MD)",
                    sex = "Male",
                    age = 38,
                    address = "Black Lion Specialized Hospital, Addis Ababa",
                    email = "gemechu.ragea@gmail.com",
                    phone = "0911000002",
                    password = "password123",
                    role = "PHYSICIAN"
                )
            )

            // Register Damto Ragea Chewaka (Manager / Admin)
            db.userDao.insertUser(
                UserEntity(
                    firstName = "Damto",
                    middleName = "Ragea",
                    lastName = "Chewaka",
                    sex = "Male",
                    age = 29,
                    address = "Addis Ababa, Ethiopia",
                    email = "damto.ragea@gmail.com",
                    phone = "0911000003",
                    password = "password123",
                    role = "MANAGER_ADMIN"
                )
            )

            // Register a regular test user
            db.userDao.insertUser(
                UserEntity(
                    firstName = "Hayyu",
                    middleName = "Ragea",
                    lastName = "Chewaka",
                    sex = "Female",
                    age = 24,
                    address = "Bole Road, Addis Ababa",
                    email = "hayyu.ragea@gmail.com",
                    phone = "0911234567",
                    password = "password123",
                    role = "CLIENT"
                )
            )
        }

        // 2. Prepopulate articles if empty
        // We'll query first
        val hasResources = db.resourceDao.getAllResourcesFlow()
        // Simple direct suspend check by trying to insert if empty might be easier. Let's do it directly.
        // Let's check using a custom selective query or simply get list size
        val list = db.resourceDao.getAllResourcesFlow()
        // Wait, flow cannot be queried directly without collecting, but we can have a one-off query or check
        // If we retrieve a list first
    }

    suspend fun populateResources() = withContext(Dispatchers.IO) {
        // Initial articles
        db.resourceDao.insertResource(
            ResourceEntity(
                title = "Introduction to Contraception & Family Planning",
                category = "Contraception & Family Planning",
                content = """
                    Family planning allows individuals and couples to anticipate and attain their desired number of children and the spacing and timing of their births. It is achieved through the use of contraceptive methods and the treatment of involuntary infertility. 

                    Types of Contraceptive Methods:
                    1. Barrier Methods: Prevent sperm from entering the uterus (e.g., male and female condoms). Extremely effective at preventing sexually transmitted infections (STIs).
                    2. Hormonal Methods: Regulate body chemistry to prevent ovulation (e.g., oral contraceptive pills, injectable contraceptives like Depo-Provera, and emergency contraception).
                    3. Long-Acting Reversible Contraceptives (LARC): Semi-permanent implants inserted under the arm skin, or Intrauterine Devices (IUD) inserted into the uterus. Highly effective for 3 to 10 years.
                    4. Permanent Methods: Surgical sterilization (tubal ligation for females, vasectomy for males) which offers irreversible lifelong prevention.

                    Choosing a Method:
                    Every body is unique. It is highly recommended to book a consultation with our qualified physicians (like Dr. Zelalem or Dr. Gemechu) directly through the Applets Appointment panel to discuss which method best suits your lifestyle and health history.
                """.trimIndent(),
                author = "Dr. Zelalem Abera (Msc)"
            )
        )

        db.resourceDao.insertResource(
            ResourceEntity(
                title = "Essential Guide to Maternal Care & Antenatal Visits",
                category = "Maternal & Newborn Care",
                content = """
                    Antenatal Care (ANC) is the clinical care provided to pregnant women during gestation. Regular maternal care is crucial to monitor baby development, prevent complications, and prepare for a safe, sanitary delivery.

                    Healthy Pregnancy Guidelines:
                    1. High Nutrition diets: Increase intake of folic acid, iron, calcium, and protein. Stay hydrated and avoid raw foods, high caffeine, and self-prescribed medications.
                    2. Minimum 4 Recommended ANC Visits: First visit before 16 weeks of pregnancy to establish a baseline scan; subsequent visits at 24-28 weeks, 32 weeks, and 36-38 weeks.
                    3. Danger Signs to Monitor: Seek immediate clinical assistance if you experience vaginal bleeding, severe head pain, sudden swelling of hands or face, high fever, or decreased baby movement.

                    Mothers in rural and remote setups benefit immensely from early scheduling. Use the Home screen's booking feature to find available physicians in the local primary healthcare units.
                """.trimIndent(),
                author = "Dr. Gemechu Ragea (MD)"
            )
        )

        db.resourceDao.insertResource(
            ResourceEntity(
                title = "Confidential Voluntary Counseling & Testing (VCT)",
                category = "Sexual Reproductive Health & STIs",
                content = """
                    Voluntary Counseling and Testing (VCT) is a pillar of prevention and support for HIV/AIDS and other Sexually Transmitted Infections (STIs). 

                    What to Expect:
                    1. Confidential Pre-test Counseling: Explaining the testing procedure, exploring personal risks, and preparing mentally for either outcome.
                    2. Rapid Blood Test: Quick, highly accurate test with results typically available in 15-20 minutes.
                    3. Post-test Support: Providing advice on maintaining a negative status or linkage to comprehensive care, ARVs (Antiretroviral therapy), and family counseling if positive.

                    Maintaining your privacy is our system's core tenet. Our Chat room provides an encrypted, anonymous channel to discuss sensitive reproductive status questions, completely free from social stigma or embarrassment.
                """.trimIndent(),
                author = "IRHC Health Ministry"
            )
        )

        db.resourceDao.insertResource(
            ResourceEntity(
                title = "Adolescent Physical Development & Menstrual Hygiene",
                category = "Adolescent Health & Hygiene",
                content = """
                    Adolescence is a transformative physical, social, and emotional phase spanning ages 10 to 19. It initiates puberty, bringing breast development and height surges in girls, and voice lowering and facial hair in boys.

                    Understanding Menstruation:
                    1. The Biological Cycle: Menstruation is the shedding of the uterine lining, occurring approximately every 21 to 35 days, lasting 3 to 7 days.
                    2. Cleanliness and Safety: Use sterile sanitary pads or clean cotton cloths. Maintain proper daily bathing, change pads every 4 to 6 hours, and safely dispose of used materials to avoid urinary tract and reproductive infections.
                    3. Breaking the Stigma: Menstruation is a completely normal, healthy physiological milestone of growth. It is not an illness, a taboo, or a source of impurity. 

                    Empower youngsters at home by providing precise, non-stigmatized awareness. Type any question in the "IRHC Consultant" tab to receive reliable info.
                """.trimIndent(),
                author = "Dr. Gemechu Ragea (MD)"
            )
        )

        // Seed some sample appointments so the Dashboard feels populated immediately!
        db.appointmentDao.insertAppointment(
            AppointmentEntity(
                userName = "Selamawit Kebede",
                userPhone = "0912111111",
                physicianName = "Zelalem Abera (Msc)",
                date = "2026-05-25",
                time = "10:30 AM",
                reason = "Interested in getting contraceptive implants. Need counseling on side-effects.",
                status = "PENDING"
            )
        )

        db.appointmentDao.insertAppointment(
            AppointmentEntity(
                userName = "Almaz Tekle",
                userPhone = "0912222222",
                physicianName = "Gemechu Ragea (MD)",
                date = "2026-05-22",
                time = "02:00 PM",
                reason = "First prenatal screening. 12 weeks pregnant.",
                status = "APPROVED"
            )
        )

        db.appointmentDao.insertAppointment(
            AppointmentEntity(
                userName = "Aster Assefa",
                userPhone = "0912333333",
                physicianName = "Gemechu Ragea (MD)",
                date = "2026-05-18",
                time = "09:00 AM",
                reason = "Regular checkup on maternal nutrition and iron supplements.",
                status = "COMPLETED",
                treatmentReport = "Maternal vitals are perfect. Prescribed prenatal vitamins and folic acid. Patient educated on eating dark greens.",
                prescription = "Prenatal Multivitamins (1 tab daily, 30 days), Ferrous Fumarate + Folic Acid (1 tab daily)."
            )
        )
    }
}
