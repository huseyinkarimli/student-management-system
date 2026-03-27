package az.developia.studentmanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import az.developia.studentmanagement.dto.StudentRequestDto;
import az.developia.studentmanagement.dto.StudentResponseDto;
import az.developia.studentmanagement.dto.StudentUpdateRequest;
import az.developia.studentmanagement.entity.StudentEntity;
import az.developia.studentmanagement.repository.StudentRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository repository;

    @Autowired
    private ModelMapper mapper;

    public List<StudentResponseDto> findAllByUserId(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(entity -> mapper.map(entity, StudentResponseDto.class))
                .collect(Collectors.toList());
    }

    public List<StudentResponseDto> findAllByUserIdSearch(Long userId, String name) {
        return repository.findByUserIdAndNameContainingIgnoreCase(userId, name).stream()
                .map(entity -> mapper.map(entity, StudentResponseDto.class))
                .collect(Collectors.toList());
    }

    public List<StudentResponseDto> findAllByUserIdSurnameSearch(Long userId, String surname) {
        return repository.findByUserIdAndSurnameContainingIgnoreCase(userId, surname).stream()
                .map(entity -> mapper.map(entity, StudentResponseDto.class))
                .collect(Collectors.toList());
    }

    public StudentResponseDto findByIdAndUserId(Long id, Long userId) {
        StudentEntity entity = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Tələbə tapılmadı, id: " + id));
        return mapper.map(entity, StudentResponseDto.class);
    }

    public void deleteByIdAndUserId(Long id, Long userId) {
        StudentEntity entity = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Tələbə tapılmadı, id: " + id));
        repository.delete(entity);
    }

    public void save(StudentRequestDto request, Long userId) {
        StudentEntity entity = mapper.map(request, StudentEntity.class);
        entity.setUserId(userId);
        entity.setCreatedAt(java.time.LocalDateTime.now());
        repository.save(entity);
    }

    public void update(Long id, StudentUpdateRequest request, Long userId) {
        StudentEntity entity = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Tələbə tapılmadı, id: " + id));

        entity.setName(request.getName());
        entity.setSurname(request.getSurname());
        entity.setEmail(request.getEmail());
        entity.setAge(request.getAge());

        repository.save(entity);
    }

    public List<StudentResponseDto> findAllLimitByUserId(Long userId, Integer begin, Integer length) {
        return repository.findAllLimitByUserId(userId, begin, length).stream()
                .map(entity -> mapper.map(entity, StudentResponseDto.class))
                .collect(Collectors.toList());
    }

    public boolean existsByUserIdAndEmail(Long userId, String email) {
        return repository.findByUserIdAndEmail(userId, email).isPresent();
    }

    public List<StudentResponseDto> findAllByUserIdAndAgeBetween(Long userId, Integer minAge, Integer maxAge) {
        return repository.findByUserIdAndAgeBetween(userId, minAge, maxAge).stream()
                .map(entity -> mapper.map(entity, StudentResponseDto.class))
                .collect(Collectors.toList());
    }


    public List<StudentResponseDto> searchByUserIdAndNameOrSurname(Long userId, String search) {
        return repository.searchByUserIdAndNameOrSurname(userId, search).stream()
                .map(entity -> mapper.map(entity, StudentResponseDto.class))
                .collect(Collectors.toList());
    }


    public Long countByUserId(Long userId) {
        return repository.countByUserId(userId);
    }
}