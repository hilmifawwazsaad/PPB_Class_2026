package com.example.crudsiswa.data.repositories

import com.example.crudsiswa.data.dao.StudentDao
import com.example.crudsiswa.data.entities.Student
import kotlinx.coroutines.flow.Flow

class StudentRepository(private val studentDao: StudentDao) {

    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()

    fun searchStudents(query: String): Flow<List<Student>> =
        studentDao.searchStudents(query)

    suspend fun getStudentById(id: Int): Student? =
        studentDao.getStudentById(id)

    suspend fun insertStudent(student: Student) =
        studentDao.insertStudent(student)

    suspend fun updateStudent(student: Student) =
        studentDao.updateStudent(student)

    suspend fun deleteStudent(student: Student) =
        studentDao.deleteStudent(student)

    suspend fun deleteStudentById(id: Int) =
        studentDao.deleteStudentById(id)
}
