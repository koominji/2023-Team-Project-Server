package com.gdscswu_server.server.domain.networking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdscswu_server.server.domain.member.domain.*;
import com.gdscswu_server.server.domain.member.dto.LoginResponseDto;
import com.gdscswu_server.server.domain.networking.domain.Bookmark;
import com.gdscswu_server.server.domain.networking.domain.BookmarkRepository;
import com.gdscswu_server.server.domain.networking.dto.FilterOptionsRequestDto;
import com.gdscswu_server.server.domain.networking.dto.GenerationResponseDto;
import com.gdscswu_server.server.domain.networking.dto.MemberResponseDto;
import com.gdscswu_server.server.domain.networking.dto.ProjectResponseDto;
import com.gdscswu_server.server.global.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.ClassBasedNavigableIterableAssert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class NetworkControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private GenerationRepository generationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private BookmarkRepository bookmarkRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ObjectMapper objectMapper;
    @Test
    @DisplayName("전체 (필터링 X) 멤버 조회 테스트")
    public void findAllMembers() throws Exception{
        // Given
        Member member1 = memberRepository.save(Member.builder()
                .googleEmail("abc@abc.com")
                .name("김슈니")
                .profileImagePath("path")
                .build());

        Member member2 = memberRepository.save(Member.builder()
                .googleEmail("def@def.com")
                .name("박슈니")
                .profileImagePath("path")
                .build());

        Generation generation1=generationRepository.save(Generation.builder()
                .member(member1)
                .number(1)
                .department("Android")
                .level("Member")
                .build());

        Generation generation2=generationRepository.save(Generation.builder()
                .member(member1)
                .number(2)
                .department("Server")
                .level("Core")
                .build());

        Generation generation3=generationRepository.save(Generation.builder()
                .member(member2)
                .number(2)
                .department("Server")
                .level("Member")
                .build());

        Project project1 = projectRepository.save(Project.builder()
                .title("김슈니의 1기 첫번째 프젝")
                .generation(generation1)
                .member(member1)
                .part("PM")
                .build());

        Project project2 = projectRepository.save(Project.builder()
                .title("김슈니의 1기 첫번째 프젝")
                .generation(generation1)
                .member(member1)
                .part("Front")
                .build());

        Project project3 = projectRepository.save(Project.builder()
                .title("김슈니의 1기 두번째 프젝")
                .generation(generation1)
                .member(member1)
                .part("Front")
                .build());

        Project project4 = projectRepository.save(Project.builder()
                .title("김슈니의 2기 첫번째 프젝")
                .generation(generation2)
                .member(member1)
                .part("Back")
                .build());

        Project project5 = projectRepository.save(Project.builder()
                .title("박슈니의 2기 첫번째 프젝")
                .generation(generation3)
                .member(member2)
                .part("Back")
                .build());

        // 예시로 몇 가지 값을 할당
        final List<String> departments = null;
        final List<String> parts = null;
        final List<String> levels = null;

        FilterOptionsRequestDto filterOptionsRequestDto= FilterOptionsRequestDto.builder()
                .parts(departments)
                .departments(parts)
                .levels(levels)
                .build();

        // When
        // 김슈니 박슈니 전부 저장
        generationRepository.saveAll(List.of(generation1, generation2, generation3));
        projectRepository.saveAll(List.of(project1, project2, project3, project4, project5));
        memberRepository.saveAll(List.of(member1,member2));
        // 북마크는 김슈니만 되어있는 상태
        bookmarkRepository.save(new Bookmark(member1, member1));

        // then
        String url = "http://localhost:" + port + "/api/v1/network";
        LoginResponseDto loginResponseDto = jwtUtil.generateTokens(member1);

        mockMvc.perform(post(url)
                        .contentType(APPLICATION_JSON).header("Authorization", "Bearer " + loginResponseDto.getAccessToken())
                .content(new ObjectMapper().writeValueAsString(filterOptionsRequestDto)))
                .andExpect(status().isOk())
                //.andExpect(jsonPath("$[1].name").value("김슈니"))
                //.andExpect(jsonPath("$[1].memberDetails[0].partList[0].part[0]").value("PM"))
                //.andExpect(jsonPath("$[1].memberDetails[0].partList[1].part[0]").value("Front"))
                .andDo(print());
    }

    @Test
    @DisplayName("필터링 멤버 조회 테스트")
    public void findFilteredMembers() throws Exception{
        // Given
        Member member1 = memberRepository.save(Member.builder()
                .googleEmail("abc@abc.com")
                .name("김슈니")
                .profileImagePath("path")
                .build());

        Member member2 = memberRepository.save(Member.builder()
                .googleEmail("def@def.com")
                .name("박슈니")
                .profileImagePath("path")
                .build());

        Generation generation1=generationRepository.save(Generation.builder()
                .member(member1)
                .number(1)
                .department("Android")
                .level("Member")
                .build());

        Generation generation2=generationRepository.save(Generation.builder()
                .member(member1)
                .number(2)
                .department("Server")
                .level("Core")
                .build());

        Generation generation3=generationRepository.save(Generation.builder()
                .member(member2)
                .number(2)
                .department("Server")
                .level("Member")
                .build());

        Project project1 = projectRepository.save(Project.builder()
                .title("김슈니의 1기 첫번째 프젝")
                .generation(generation1)
                .member(member1)
                .part("PM")
                .build());

        Project project2 = projectRepository.save(Project.builder()
                .title("김슈니의 1기 첫번째 프젝")
                .generation(generation1)
                .member(member1)
                .part("Front")
                .build());

        Project project3 = projectRepository.save(Project.builder()
                .title("김슈니의 1기 두번째 프젝")
                .generation(generation1)
                .member(member1)
                .part("Front")
                .build());

        Project project4 = projectRepository.save(Project.builder()
                .title("김슈니의 2기 첫번째 프젝")
                .generation(generation2)
                .member(member1)
                .part("Back")
                .build());

        Project project5 = projectRepository.save(Project.builder()
                .title("박슈니의 2기 첫번째 프젝")
                .generation(generation3)
                .member(member2)
                .part("Back")
                .build());

        // 예시로 몇 가지 값을 할당
        final List<String> departments = Arrays.asList("Server");
        //final List<String> parts = Arrays.asList("Back");
        //final List<String> levels = Arrays.asList("Core");

        // 예시로 몇 가지 값을 할당
        //final List<String> departments = null;
        final List<String> parts = null;
        final List<String> levels = null;

        FilterOptionsRequestDto filterOptionsRequestDto= FilterOptionsRequestDto.builder()
                .parts(parts)
                .departments(departments)
                .levels(levels)
                .build();

        // When
        // 김슈니 박슈니 전부 저장
        generationRepository.saveAll(List.of(generation1, generation2, generation3));
        projectRepository.saveAll(List.of(project1, project2, project3, project4, project5));
        memberRepository.saveAll(List.of(member1,member2));
        // 북마크는 김슈니만 되어있는 상태
        bookmarkRepository.save(new Bookmark(member1, member1));

        // then
        String url = "http://localhost:" + port + "/api/v1/network";
        LoginResponseDto loginResponseDto = jwtUtil.generateTokens(member1);

        mockMvc.perform(post(url)
                        .contentType(APPLICATION_JSON).header("Authorization", "Bearer " + loginResponseDto.getAccessToken())
                        .content(new ObjectMapper().writeValueAsString(filterOptionsRequestDto)))
                .andExpect(status().isOk())
                //.andExpect(jsonPath("$[1].name").value("김슈니"))
                //.andExpect(jsonPath("$[1].memberDetails[0].partList[0].part[0]").value("PM"))
                //.andExpect(jsonPath("$[1].memberDetails[0].partList[1].part[0]").value("Front"))
                .andDo(print());
    }



    @Test
    @DisplayName("멤버 북마크 기능 설정 테스트")
    public void setBookmark() throws Exception{
        //given
        Member member1 = memberRepository.save(Member.builder()
                .googleEmail("abc@abc.com")
                .name("김우동")
                .profileImagePath("path")
                .build());
        Generation generation1=generationRepository.save(Generation.builder()
                .member(member1)
                .number(3)
                .department("Android")
                .level("Member")
                .build());
        Project project1 = projectRepository.save(Project.builder()
                .title("김우동의 프젝")
                .generation(generation1)
                .member(member1)
                .part("PM")
                .build());

        // when
        // 디비에 김우동 저장
        generationRepository.saveAll(List.of(generation1));
        projectRepository.saveAll(List.of(project1));
        memberRepository.save(member1);
        // 북마크가 설정되는 걸 테스트 하기 위해 북마크 레포에는 저장하지 않음

        Long memberId = member1.getId();
        String url = "http://localhost:" + port + "/api/v1/network/bookmark/"+memberId;
        LoginResponseDto loginResponseDto = jwtUtil.generateTokens(member1);

        // then
        mockMvc.perform(post(url)
                        .contentType(APPLICATION_JSON).header("Authorization", "Bearer " + loginResponseDto.getAccessToken()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("멤버 북마크 기능 해제 테스트")
    public void removeBookmark() throws Exception{
        //given
        Member member1 = memberRepository.save(Member.builder()
                .googleEmail("abc@abc.com")
                .name("김우동")
                .profileImagePath("path")
                .build());
        Generation generation1=generationRepository.save(Generation.builder()
                .member(member1)
                .number(3)
                .department("Android")
                .level("Member")
                .build());
        Project project1 = projectRepository.save(Project.builder()
                .title("김우동의 프젝")
                .generation(generation1)
                .member(member1)
                .part("PM")
                .build());

        // When
        // 디비에 김우동 저장
        generationRepository.saveAll(List.of(generation1));
        projectRepository.saveAll(List.of(project1));
        memberRepository.save(member1);
        // 김우동이 이미 북마크 레포에 있어야 해서 북마크 레포에 김우동 생성해서 저장
        bookmarkRepository.save(new Bookmark(member1, member1));

        Long memberId = member1.getId();
        String url = "http://localhost:" + port + "/api/v1/network/bookmark/"+memberId;
        LoginResponseDto loginResponseDto = jwtUtil.generateTokens(member1);

        // then
        mockMvc.perform(post(url)
                        .contentType(APPLICATION_JSON).header("Authorization", "Bearer " + loginResponseDto.getAccessToken()))
                .andExpect(status().isOk())
                .andDo(print());
    }
}