package com.gdscswu_server.server.domain.member.controller;

import com.gdscswu_server.server.domain.member.domain.Member;
import com.gdscswu_server.server.domain.member.domain.MemberRepository;
import com.gdscswu_server.server.domain.model.TokenClaimVo;
import com.gdscswu_server.server.global.util.JwtUtil;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerTest {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    @Test
    @DisplayName("findById() Test")
    public void findById() throws Exception {
        // given
        // 정보를 보고 싶은 멤버
        Member member = Member.builder()
                .googleEmail("googleEmail")
                .name("name")
                .profileImagePath("profileImagePath")
                .build();
        memberRepository.save(member);

        // 정보를 보려는 멤버
        Member member2 = Member.builder()
                .googleEmail("googleEmail")
                .name("name")
                .profileImagePath("profileImagePath")
                .build();
        memberRepository.save(member2);

        TokenClaimVo vo = new TokenClaimVo(member2.getId(), member2.getEmail());
        String accessToken = jwtUtil.generateToken(true, vo);

        // mockMvc 요청 구성
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/v1/member/{id}", member.getId())
                .header("Authorization", "Bearer " + accessToken);  // AccessToken 추가

        // mockMvc 실행 및 결과 확인
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }
}
